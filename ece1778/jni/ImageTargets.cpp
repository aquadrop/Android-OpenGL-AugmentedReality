/*==============================================================================
 Copyright (c) 2010-2013 QUALCOMM Austria Research Center GmbH.
 All Rights Reserved.
 Qualcomm Confidential and Proprietary
 
 @file
 ImageTargets.cpp
 
 @brief
 Sample for ImageTargets
 
 ==============================================================================*/


#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <iostream>
#include <vector>

using namespace std;

#ifdef USE_OPENGL_ES_1_1
#include <GLES/gl.h>
#include <GLES/glext.h>
#else
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#endif

#include <QCAR/QCAR.h>
#include <QCAR/CameraDevice.h>
#include <QCAR/Renderer.h>
#include <QCAR/VideoBackgroundConfig.h>
#include <QCAR/Trackable.h>
#include <QCAR/TrackableResult.h>
#include <QCAR/Tool.h>
#include <QCAR/Tracker.h>
#include <QCAR/TrackerManager.h>
#include <QCAR/ImageTracker.h>
#include <QCAR/CameraCalibration.h>
#include <QCAR/UpdateCallback.h>
#include <QCAR/DataSet.h>


#include "SampleUtils.h"
#include "Texture.h"
#include "CubeShaders.h"
#include "LightShaders.h"
#include "Teapot.h"
#include "Light.h"


#ifdef __cplusplus
extern "C"
{
#endif
    
    // Textures:
    int textureCount                = 0;
    Texture** textures              = 0;
    
    // OpenGL ES 2.0 specific:
#ifdef USE_OPENGL_ES_2_0
    unsigned int shaderProgramID    = 0;
    GLint vertexHandle              = 0;
    GLint normalHandle              = 0;
    GLint textureCoordHandle        = 0;
    GLint mvpMatrixHandle           = 0;
    GLint texSampler2DHandle        = 0;
    
    //light handle from learn openGLES tutorial lesson two
    GLint mMVMatrixHandle           = 0;
    GLint mLightPosHandle           = 0;
    GLint mcolorHandle              = 0;
    GLint mPositionHandle           = 0;
    GLint pointVertexShaderHandle   = 0;
    GLint pointFragmentShaderHandle = 0;
    unsigned
        int mPointProgramHandle     = 0;
    GLint pointMVPMatrixHandle      = 0;
    GLint pointPositionHandle       = 0;
    
#endif
    
    // Screen dimensions:
    unsigned int screenWidth        = 0;
    unsigned int screenHeight       = 0;
    
    // Indicates whether screen is in portrait (true) or landscape (false) mode
    bool isActivityInPortraitMode   = false;
    
    // The projection matrix used for rendering virtual objects:
    QCAR::Matrix44F projectionMatrix;
    
    // Constants:
    static const float kObjectScale = 15.f;
    
    QCAR::DataSet* dataSetStonesAndChips    = 0;
    QCAR::DataSet* dataSetTarmac            = 0;
    
    bool switchDataSetAsap          = false;
    
    
    
    /** parameters for accepting java image object, vertices, normals and (indices). */
    // whether use gldrawelements or gldrawarray
    bool has_input = false;
    std::vector<float*> orig_list_vertex_cor_tri;
    std::vector<short> list_vertex_length;
    std::vector<float*> orig_list_normal_cor_tri;
    std::vector<short> list_normal_length;
    
    std::vector<float*> list_vertex_cor_tri;
    std::vector<float*> list_normal_cor_tri;
    std::vector<float*> list_color;
    std::vector<float> list_color_length;
    
    float scale_factor = 1.0f;
    float x_angle = 0.0f;
    float y_angle = 0.0f;
    
    float mLightPosInEyeSpace[4];
    float mLightPosInWorldSpace[4];
    float mLightPosInModelSpace[4] = {0.0f, 0.0f, 10.0f, 1.0f};
    float mLightModelMatrix[16];
    float viewMatrix[16];
    
    // Object to receive update callbacks from QCAR SDK
    class ImageTargets_UpdateCallback : public QCAR::UpdateCallback
    {
        virtual void QCAR_onUpdate(QCAR::State& /*state*/)
        {
            if (switchDataSetAsap)
            {
                switchDataSetAsap = false;
                
                // Get the image tracker:
                QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
                QCAR::ImageTracker* imageTracker = static_cast<QCAR::ImageTracker*>(
                                                                                    trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER));
                if (imageTracker == 0 || dataSetStonesAndChips == 0 || dataSetTarmac == 0 ||
                    imageTracker->getActiveDataSet() == 0)
                {
                    LOG("Failed to switch data set.");
                    return;
                }
                
                if (imageTracker->getActiveDataSet() == dataSetStonesAndChips)
                {
                    imageTracker->deactivateDataSet(dataSetStonesAndChips);
                    imageTracker->activateDataSet(dataSetTarmac);
                }
                else
                {
                    imageTracker->deactivateDataSet(dataSetTarmac);
                    imageTracker->activateDataSet(dataSetStonesAndChips);
                }
            }
        }
    };
    
    ImageTargets_UpdateCallback updateCallback;
    
    JNIEXPORT int JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_getOpenGlEsVersionNative(JNIEnv *, jobject)
    {
#ifdef USE_OPENGL_ES_1_1
        return 1;
#else
        return 2;
#endif
    }
    
    
    JNIEXPORT void JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_setActivityPortraitMode(JNIEnv *, jobject, jboolean isPortrait)
    {
        isActivityInPortraitMode = isPortrait;
    }
    
    
    
    JNIEXPORT void JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_switchDatasetAsap(JNIEnv *, jobject)
    {
        switchDataSetAsap = true;
    }
    
    
    JNIEXPORT int JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_initTracker(JNIEnv *, jobject)
    {
        LOG("Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_initTracker");
        
        // Initialize the image tracker:
        QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
        QCAR::Tracker* tracker = trackerManager.initTracker(QCAR::Tracker::IMAGE_TRACKER);
        if (tracker == NULL)
        {
            LOG("Failed to initialize ImageTracker.");
            return 0;
        }
        
        LOG("Successfully initialized ImageTracker.");
        return 1;
    }
    
    
    JNIEXPORT void JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_deinitTracker(JNIEnv *, jobject)
    {
        LOG("Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_deinitTracker");
        
        // Deinit the image tracker:
        QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
        trackerManager.deinitTracker(QCAR::Tracker::IMAGE_TRACKER);
    }
    
    
    JNIEXPORT int JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_initObjectNative(JNIEnv* env, jobject, jfloatArray vertex, jfloatArray normal, jfloatArray color)
    {
        // Step 1: Convert
        jfloat* floatCArray = (env)->GetFloatArrayElements(vertex, NULL);
        if (floatCArray == NULL) -1;
        jsize length = (env)->GetArrayLength(vertex);
        list_vertex_length.push_back((short) length);
        
        //Step 2: Accept
        list_vertex_cor_tri.push_back(floatCArray);
        orig_list_vertex_cor_tri.push_back(floatCArray);
        
        // Step 1: Convert
        floatCArray = (env)->GetFloatArrayElements(normal, NULL);
        if (floatCArray == NULL) -1;
        length = (env)->GetArrayLength(normal);
        list_normal_length.push_back((short) length);
        
        //Step 2: Accept
        list_normal_cor_tri.push_back(floatCArray);
        
        // Step 1: Convert
        floatCArray = (env)->GetFloatArrayElements(color, NULL);
        if (floatCArray == NULL) -1;
        length = (env)->GetArrayLength(color);
        list_color_length.push_back((short) length);
        
        //Step 2: Accept
        list_color.push_back(floatCArray);
        
        has_input = true;
        return 0;
    }
    
    JNIEXPORT float JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_testStoredVectorsNative(JNIEnv* env, jobject)
    {
        return (float) list_vertex_cor_tri.at(0)[0];
    }
    
    JNIEXPORT void JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_setHasInputNative(JNIEnv* env, jobject, jboolean _has_input)
    {
        has_input = _has_input;
    }
    
    JNIEXPORT void JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_destroyStoredVectorsNative(JNIEnv* env, jobject)
    {
        list_normal_cor_tri.clear();
        orig_list_normal_cor_tri.clear();
        list_normal_length.clear();
        list_vertex_cor_tri.clear();
        orig_list_vertex_cor_tri.clear();
        list_vertex_length.clear();
        list_color.clear();
        list_color_length.clear();
        
        has_input = false;
    }
    JNIEXPORT int JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_loadTrackerData(JNIEnv *, jobject)
    {
        LOG("Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_loadTrackerData");
        
        // Get the image tracker:
        QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
        QCAR::ImageTracker* imageTracker = static_cast<QCAR::ImageTracker*>(
                                                                            trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER));
        if (imageTracker == NULL)
        {
            LOG("Failed to load tracking data set because the ImageTracker has not"
                " been initialized.");
            return 0;
        }
        
        // Create the data sets:
        dataSetStonesAndChips = imageTracker->createDataSet();
        if (dataSetStonesAndChips == 0)
        {
            LOG("Failed to create a new tracking data.");
            return 0;
        }
        
        dataSetTarmac = imageTracker->createDataSet();
        if (dataSetTarmac == 0)
        {
            LOG("Failed to create a new tracking data.");
            return 0;
        }
        
        // Load the data sets:
        if (!dataSetStonesAndChips->load("StonesAndChips.xml", QCAR::DataSet::STORAGE_APPRESOURCE))
        {
            LOG("Failed to load data set.");
            return 0;
        }
        
        if (!dataSetTarmac->load("Tarmac.xml", QCAR::DataSet::STORAGE_APPRESOURCE))
        {
            LOG("Failed to load data set.");
            return 0;
        }
        
        // Activate the data set:
        if (!imageTracker->activateDataSet(dataSetStonesAndChips))
        {
            LOG("Failed to activate data set.");
            return 0;
        }
        
        LOG("Successfully loaded and activated data set.");
        return 1;
    }
    
    
    JNIEXPORT int JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_destroyTrackerData(JNIEnv *, jobject)
    {
        LOG("Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_destroyTrackerData");
        
        // Get the image tracker:
        QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
        QCAR::ImageTracker* imageTracker = static_cast<QCAR::ImageTracker*>(
                                                                            trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER));
        if (imageTracker == NULL)
        {
            LOG("Failed to destroy the tracking data set because the ImageTracker has not"
                " been initialized.");
            return 0;
        }
        
        if (dataSetStonesAndChips != 0)
        {
            if (imageTracker->getActiveDataSet() == dataSetStonesAndChips &&
                !imageTracker->deactivateDataSet(dataSetStonesAndChips))
            {
                LOG("Failed to destroy the tracking data set StonesAndChips because the data set "
                    "could not be deactivated.");
                return 0;
            }
            
            if (!imageTracker->destroyDataSet(dataSetStonesAndChips))
            {
                LOG("Failed to destroy the tracking data set StonesAndChips.");
                return 0;
            }
            
            LOG("Successfully destroyed the data set StonesAndChips.");
            dataSetStonesAndChips = 0;
        }
        
        if (dataSetTarmac != 0)
        {
            if (imageTracker->getActiveDataSet() == dataSetTarmac &&
                !imageTracker->deactivateDataSet(dataSetTarmac))
            {
                LOG("Failed to destroy the tracking data set Tarmac because the data set "
                    "could not be deactivated.");
                return 0;
            }
            
            if (!imageTracker->destroyDataSet(dataSetTarmac))
            {
                LOG("Failed to destroy the tracking data set Tarmac.");
                return 0;
            }
            
            LOG("Successfully destroyed the data set Tarmac.");
            dataSetTarmac = 0;
        }
        
        return 1;
    }
    
    
    JNIEXPORT void JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_onQCARInitializedNative(JNIEnv *, jobject)
    {
        // Register the update callback where we handle the data set swap:
        QCAR::registerCallback(&updateCallback);
        
        // Comment in to enable tracking of up to 2 targets simultaneously and
        // split the work over multiple frames:
        // QCAR::setHint(QCAR::HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS, 2);
    }
    
    
    JNIEXPORT void JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargetsRenderer_renderFrame(JNIEnv *, jobject)
    {
        //LOG("Java_com_qualcomm_QCARSamples_ImageTargets_GLRenderer_renderFrame");
        
        // Clear color and depth buffer
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        // Get the state from QCAR and mark the beginning of a rendering section
        QCAR::State state = QCAR::Renderer::getInstance().begin();
        
        // Explicitly render the Video Background
        QCAR::Renderer::getInstance().drawVideoBackground();
        
#ifdef USE_OPENGL_ES_1_1
        // Set GL11 flags:
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_NORMAL_ARRAY);
        //glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        
        //glEnable(GL_TEXTURE_2D);
        
        
        //Ligth
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glLightfv(GL_LIGHT0, GL_DIFFUSE, diffuse);
        glLightfv(GL_LIGHT0, GL_AMBIENT, ambient_color);
        glLightfv(GL_LIGHT0, GL_POSITION, pos0);
        //    glDisable(GL_LIGHTING);
        
#endif
        
        // We must detect if background reflection is active and adjust the culling direction.
        // If the reflection is active, this means the post matrix has been reflected as well,
        // therefore standard counter clockwise face culling will result in "inside out" models.
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        if(QCAR::Renderer::getInstance().getVideoBackgroundConfig().mReflection == QCAR::VIDEO_BACKGROUND_REFLECTION_ON)
            glFrontFace(GL_CW);  //Front camera
        else
            glFrontFace(GL_CCW);   //Back camera
        
        
        // Did we find any trackables this frame?
        for(int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
        {
            // Get the trackable:
            const QCAR::TrackableResult* result = state.getTrackableResult(tIdx);
            const QCAR::Trackable& trackable = result->getTrackable();
            QCAR::Matrix44F modelViewMatrix =
            QCAR::Tool::convertPose2GLMatrix(result->getPose());
            
            // Choose the texture based on the target name:
            int textureIndex;
            if (strcmp(trackable.getName(), "chips") == 0)
            {
                textureIndex = 0;
            }
            else if (strcmp(trackable.getName(), "stones") == 0)
            {
                textureIndex = 1;
            }
            else
            {
                textureIndex = 2;
            }
            
            const Texture* const thisTexture = textures[textureIndex];
            
#ifdef USE_OPENGL_ES_1_1
            // Load projection matrix:
            glMatrixMode(GL_PROJECTION);
            glLoadMatrixf(projectionMatrix.data);
            
            // Load model view matrix:
            glMatrixMode(GL_MODELVIEW);
            glLoadMatrixf(modelViewMatrix.data);
            glTranslatef(0.f, 0.f, kObjectScale);
            glScalef(kObjectScale, kObjectScale, kObjectScale);
            glScalef(scale_factor, scale_factor, scale_factor);
            glRotatef(x_angle, 0, 1, 0);
            glRotatef(y_angle, 1, 0, 0);
            glShadeModel(GL_SMOOTH);
            
            // Draw object:
            //glBindTexture(GL_TEXTURE_2D, thisTexture->mTextureID);
            //glTexCoordPointer(2, GL_FLOAT, 0, (const GLvoid*) &teapotTexCoords[0]);
            if (!has_input)
            {
                glVertexPointer(3, GL_FLOAT, 0, (const GLvoid*) &teapotVertices[0]);
                glNormalPointer(GL_FLOAT, 0,  (const GLvoid*) &teapotNormals[0]);
                glDrawElements(GL_TRIANGLES, NUM_TEAPOT_OBJECT_INDEX, GL_UNSIGNED_SHORT,
                               (const GLvoid*) &teapotIndices[0]);
            }
            else
            {
                for (short i = 0; i<list_vertex_cor_tri.size(); i++)
                {
                    glVertexPointer(3, GL_FLOAT, 0, (const GLvoid*) &list_vertex_cor_tri.at(i)[0]);
                    glNormalPointer(GL_FLOAT, 0,  (const GLvoid*) &list_normal_cor_tri.at(i)[0]);
                    glDrawArrays(GL_TRIANGLES, 0, list_vertex_length.at(i)/3);
                }
            }
            
            //        glDrawArrays(GL_TRIANGLES, 0, NUM_TEAPOT_OBJECT_INDEX/3);
#else
            
            QCAR::Matrix44F modelViewProjection;
            
            SampleUtils::translatePoseMatrix(0.0f, 0.0f, kObjectScale,
                                             &modelViewMatrix.data[0]);
            SampleUtils::scalePoseMatrix(kObjectScale, kObjectScale, kObjectScale,
                                         &modelViewMatrix.data[0]);
            SampleUtils::scalePoseMatrix(scale_factor, scale_factor, scale_factor, &modelViewMatrix.data[0]);
            SampleUtils::rotatePoseMatrix(x_angle, 0, 1, 0, &modelViewMatrix.data[0]);
            SampleUtils::rotatePoseMatrix(y_angle, 1, 0, 1, &modelViewMatrix.data[0]);
            SampleUtils::multiplyMatrix(&projectionMatrix.data[0],
                                        &modelViewMatrix.data[0] ,
                                        &modelViewProjection.data[0]);
            
            
            // Calculate position of the light. Rotate and then push into the distance.
            SampleUtils::setIdentityM(&mLightModelMatrix[0]);
            SampleUtils::translatePoseMatrix(0.0f, 0.0f, 3, &mLightModelMatrix[0]);
            SampleUtils::rotatePoseMatrix(x_angle, 0, 1, 0, &mLightModelMatrix[0]);
            SampleUtils::rotatePoseMatrix(y_angle, 1, 0, 0, &mLightModelMatrix[0]);
            SampleUtils::translatePoseMatrix(0.0f, 0.0f, 2.0, &mLightModelMatrix[0]);
            
            SampleUtils::multipleMV(&mLightPosInWorldSpace[0], &mLightModelMatrix[0], &mLightPosInModelSpace[0]);
            SampleUtils::multipleMV(&mLightPosInEyeSpace[0], &modelViewMatrix.data[0], &mLightPosInModelSpace[0]);
            glUseProgram(shaderProgramID);
            
            glUniform1i(texSampler2DHandle, 0 /*GL_TEXTURE0*/);
            glUniformMatrix4fv(mvpMatrixHandle, 1, GL_FALSE,
                               (GLfloat*)&modelViewProjection.data[0] );
            glUniformMatrix4fv(mMVMatrixHandle, 1, GL_FALSE, (GLfloat*)&modelViewMatrix.data[0]);
            glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);
            
            
            
            if (!has_input)
            {
                glVertexAttribPointer(vertexHandle, 3, GL_FLOAT, GL_FALSE, 0,
                                      (const GLvoid*) &teapotVertices[0]);
                glVertexAttribPointer(normalHandle, 3, GL_FLOAT, GL_FALSE, 0,
                                      (const GLvoid*) &teapotNormals[0]);
                //        glVertexAttribPointer(textureCoordHandle, 2, GL_FLOAT, GL_FALSE, 0,
                //                              (const GLvoid*) &teapotTexCoords[0]);
                glEnableVertexAttribArray(vertexHandle);
                glEnableVertexAttribArray(normalHandle);
                //glEnableVertexAttribArray(textureCoordHandle);
                
                //glActiveTexture(GL_TEXTURE0);
                //glBindTexture(GL_TEXTURE_2D, thisTexture->mTextureID);
                glUniform1i(texSampler2DHandle, 0 /*GL_TEXTURE0*/);
                glUniformMatrix4fv(mvpMatrixHandle, 1, GL_FALSE,
                                   (GLfloat*)&modelViewProjection.data[0] );
                glUniformMatrix4fv(mMVMatrixHandle, 1, GL_FALSE, (GLfloat*)&modelViewMatrix.data[0]);
                glDrawElements(GL_TRIANGLES, NUM_TEAPOT_OBJECT_INDEX, GL_UNSIGNED_SHORT,
                               (const GLvoid*) &teapotIndices[0]);
            }
            else
            {
                
                
                
                for (short i = 0; i<list_vertex_cor_tri.size(); i++)
                {
                    glVertexAttribPointer(vertexHandle, 3, GL_FLOAT, GL_FALSE, 0,
                                          (const GLvoid*) &list_vertex_cor_tri.at(i)[0]);
                    glVertexAttribPointer(normalHandle, 3, GL_FLOAT, GL_FALSE, 0,
                                          (const GLvoid*) &list_normal_cor_tri.at(i)[0]);
                    //        glVertexAttribPointer(textureCoordHandle, 2, GL_FLOAT, GL_FALSE, 0,
                    //                              (const GLvoid*) &teapotTexCoords[0]);
                    glVertexAttribPointer(mcolorHandle, 4, GL_FLOAT, GL_FALSE, 0, (const GLvoid*) &list_color.at(i)[0]);
                    glEnableVertexAttribArray(vertexHandle);
                    glEnableVertexAttribArray(normalHandle);
                    glEnableVertexAttribArray(mcolorHandle);
                    //glEnableVertexAttribArray(textureCoordHandle);
                    
                    //glActiveTexture(GL_TEXTURE0);
                    //glBindTexture(GL_TEXTURE_2D, thisTexture->mTextureID);
                    
                    glDrawArrays(GL_TRIANGLES, 0, list_vertex_length.at(i)/3);
                }
                
                //draw point light
                glUseProgram(mPointProgramHandle);
                glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2]);
                glDisableVertexAttribArray(pointPositionHandle);
                glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, (GLfloat*) &modelViewProjection.data[0]);
                //glDrawArrays(GL_POINTS, 0, 1);
            }
            
            
            
            SampleUtils::checkGlError("ImageTargets renderFrame");
            
            //has_input = false;
#endif
            
        }
        
        glDisable(GL_DEPTH_TEST);
        
#ifdef USE_OPENGL_ES_1_1
        glDisable(GL_TEXTURE_2D);
        glDisableClientState(GL_VERTEX_ARRAY);
        glDisableClientState(GL_NORMAL_ARRAY);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
#else
        glDisableVertexAttribArray(vertexHandle);
        glDisableVertexAttribArray(normalHandle);
        glDisableVertexAttribArray(mcolorHandle);
        //glDisableVertexAttribArray(textureCoordHandle);
#endif
        
        QCAR::Renderer::getInstance().end();
    }
    
    
    void
    configureVideoBackground()
    {
        // Get the default video mode:
        QCAR::CameraDevice& cameraDevice = QCAR::CameraDevice::getInstance();
        QCAR::VideoMode videoMode = cameraDevice.
        getVideoMode(QCAR::CameraDevice::MODE_DEFAULT);
        
        
        // Configure the video background
        QCAR::VideoBackgroundConfig config;
        config.mEnabled = true;
        config.mSynchronous = true;
        config.mPosition.data[0] = 0.0f;
        config.mPosition.data[1] = 0.0f;
        
        if (isActivityInPortraitMode)
        {
            //LOG("configureVideoBackground PORTRAIT");
            config.mSize.data[0] = videoMode.mHeight
            * (screenHeight / (float)videoMode.mWidth);
            config.mSize.data[1] = screenHeight;
            
            if(config.mSize.data[0] < screenWidth)
            {
                LOG("Correcting rendering background size to handle missmatch between screen and video aspect ratios.");
                config.mSize.data[0] = screenWidth;
                config.mSize.data[1] = screenWidth *
                (videoMode.mWidth / (float)videoMode.mHeight);
            }
        }
        else
        {
            //LOG("configureVideoBackground LANDSCAPE");
            config.mSize.data[0] = screenWidth;
            config.mSize.data[1] = videoMode.mHeight
            * (screenWidth / (float)videoMode.mWidth);
            
            if(config.mSize.data[1] < screenHeight)
            {
                LOG("Correcting rendering background size to handle missmatch between screen and video aspect ratios.");
                config.mSize.data[0] = screenHeight
                * (videoMode.mWidth / (float)videoMode.mHeight);
                config.mSize.data[1] = screenHeight;
            }
        }
        
        LOG("Configure Video Background : Video (%d,%d), Screen (%d,%d), mSize (%d,%d)", videoMode.mWidth, videoMode.mHeight, screenWidth, screenHeight, config.mSize.data[0], config.mSize.data[1]);
        
        // Set the config:
        QCAR::Renderer::getInstance().setVideoBackgroundConfig(config);
    }
    
    
    JNIEXPORT void JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_initApplicationNative(
                                                                                  JNIEnv* env, jobject obj, jint width, jint height)
    {
        LOG("Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_initApplicationNative");
        
        // Store screen dimensions
        screenWidth = width;
        screenHeight = height;
        
        // Handle to the activity class:
        jclass activityClass = env->GetObjectClass(obj);
        
        jmethodID getTextureCountMethodID = env->GetMethodID(activityClass,
                                                             "getTextureCount", "()I");
        if (getTextureCountMethodID == 0)
        {
            LOG("Function getTextureCount() not found.");
            return;
        }
        
        textureCount = env->CallIntMethod(obj, getTextureCountMethodID);
        if (!textureCount)
        {
            LOG("getTextureCount() returned zero.");
            return;
        }
        
        textures = new Texture*[textureCount];
        
        jmethodID getTextureMethodID = env->GetMethodID(activityClass,
                                                        "getTexture", "(I)Lcom/qualcomm/QCARSamples/ImageTargets/Texture;");
        
        if (getTextureMethodID == 0)
        {
            LOG("Function getTexture() not found.");
            return;
        }
        
        // Register the textures
        for (int i = 0; i < textureCount; ++i)
        {
            
            jobject textureObject = env->CallObjectMethod(obj, getTextureMethodID, i);
            if (textureObject == NULL)
            {
                LOG("GetTexture() returned zero pointer");
                return;
            }
            
            textures[i] = Texture::create(env, textureObject);
        }
        LOG("Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_initApplicationNative finished");
    }
    
    
    JNIEXPORT void JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_deinitApplicationNative(
                                                                                    JNIEnv* env, jobject obj)
    {
        LOG("Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_deinitApplicationNative");
        
        // Release texture resources
        if (textures != 0)
        {
            for (int i = 0; i < textureCount; ++i)
            {
                delete textures[i];
                textures[i] = NULL;
            }
            
            delete[]textures;
            textures = NULL;
            
            textureCount = 0;
        }
    }
    
    
    JNIEXPORT void JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_startCamera(JNIEnv *,
                                                                        jobject)
    {
        LOG("Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_startCamera");
        
        // Select the camera to open, set this to QCAR::CameraDevice::CAMERA_FRONT
        // to activate the front camera instead.
        QCAR::CameraDevice::CAMERA camera = QCAR::CameraDevice::CAMERA_DEFAULT;
        
        // Initialize the camera:
        if (!QCAR::CameraDevice::getInstance().init(camera))
            return;
        
        // Configure the video background
        configureVideoBackground();
        
        // Select the default mode:
        if (!QCAR::CameraDevice::getInstance().selectVideoMode(
                                                               QCAR::CameraDevice::MODE_DEFAULT))
            return;
        
        // Start the camera:
        if (!QCAR::CameraDevice::getInstance().start())
            return;
        
        // Uncomment to enable flash
        //if(QCAR::CameraDevice::getInstance().setFlashTorchMode(true))
        //	LOG("IMAGE TARGETS : enabled torch");
        
        // Uncomment to enable infinity focus mode, or any other supported focus mode
        // See CameraDevice.h for supported focus modes
        //if(QCAR::CameraDevice::getInstance().setFocusMode(QCAR::CameraDevice::FOCUS_MODE_INFINITY))
        //	LOG("IMAGE TARGETS : enabled infinity focus");
        
        // Start the tracker:
        QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
        QCAR::Tracker* imageTracker = trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER);
        if(imageTracker != 0)
            imageTracker->start();
    }
    
    
    JNIEXPORT void JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_stopCamera(JNIEnv *, jobject)
    {
        LOG("Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_stopCamera");
        
        // Stop the tracker:
        QCAR::TrackerManager& trackerManager = QCAR::TrackerManager::getInstance();
        QCAR::Tracker* imageTracker = trackerManager.getTracker(QCAR::Tracker::IMAGE_TRACKER);
        if(imageTracker != 0)
            imageTracker->stop();
        
        QCAR::CameraDevice::getInstance().stop();
        QCAR::CameraDevice::getInstance().deinit();
    }
    
    
    JNIEXPORT void JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_setProjectionMatrix(JNIEnv *, jobject)
    {
        LOG("Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_setProjectionMatrix");
        
        // Cache the projection matrix:
        const QCAR::CameraCalibration& cameraCalibration =
        QCAR::CameraDevice::getInstance().getCameraCalibration();
        projectionMatrix = QCAR::Tool::getProjectionGL(cameraCalibration, 2.0f, 2500.0f);
    }
    
    // ----------------------------------------------------------------------------
    // Activates Camera Flash
    // ----------------------------------------------------------------------------
    JNIEXPORT jboolean JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_activateFlash(JNIEnv*, jobject, jboolean flash)
    {
        return QCAR::CameraDevice::getInstance().setFlashTorchMode((flash==JNI_TRUE)) ? JNI_TRUE : JNI_FALSE;
    }
    
    JNIEXPORT jboolean JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_autofocus(JNIEnv*, jobject)
    {
        return QCAR::CameraDevice::getInstance().setFocusMode(QCAR::CameraDevice::FOCUS_MODE_TRIGGERAUTO) ? JNI_TRUE : JNI_FALSE;
    }
    
    
    JNIEXPORT jboolean JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_setFocusMode(JNIEnv*, jobject, jint mode)
    {
        int qcarFocusMode;
        
        switch ((int)mode)
        {
            case 0:
                qcarFocusMode = QCAR::CameraDevice::FOCUS_MODE_NORMAL;
                break;
                
            case 1:
                qcarFocusMode = QCAR::CameraDevice::FOCUS_MODE_CONTINUOUSAUTO;
                break;
                
            case 2:
                qcarFocusMode = QCAR::CameraDevice::FOCUS_MODE_INFINITY;
                break;
                
            case 3:
                qcarFocusMode = QCAR::CameraDevice::FOCUS_MODE_MACRO;
                break;
                
            default:
                return JNI_FALSE;
        }
        
        return QCAR::CameraDevice::getInstance().setFocusMode(qcarFocusMode) ? JNI_TRUE : JNI_FALSE;
    }
    
    
    JNIEXPORT void JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargetsRenderer_initRendering(
                                                                                  JNIEnv* env, jobject obj)
    {
        LOG("Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargetsRenderer_initRendering");
        
        // Define clear color
        glClearColor(0.0f, 0.0f, 0.0f, QCAR::requiresAlpha() ? 0.0f : 1.0f);
        
        // Now generate the OpenGL texture objects and add settings
        for (int i = 0; i < textureCount; ++i)
        {
            glGenTextures(1, &(textures[i]->mTextureID));
            glBindTexture(GL_TEXTURE_2D, textures[i]->mTextureID);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textures[i]->mWidth,
                         textures[i]->mHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                         (GLvoid*)  textures[i]->mData);
        }
#ifndef USE_OPENGL_ES_1_1
        
//        shaderProgramID     = SampleUtils::createProgramFromBuffer(cubeMeshVertexShader,
//                                                                   cubeFragmentShader);
//        
//        vertexHandle        = glGetAttribLocation(shaderProgramID,
//                                                  "vertexPosition");
//        normalHandle        = glGetAttribLocation(shaderProgramID,
//                                                  "vertexNormal");
//        //    textureCoordHandle  = glGetAttribLocation(shaderProgramID,
//        //                                                "vertexTexCoord");
//        mvpMatrixHandle     = glGetUniformLocation(shaderProgramID,
//                                                   "modelViewProjectionMatrix");
//        texSampler2DHandle  = glGetUniformLocation(shaderProgramID, 
//                                                   "texSampler2D");
        

//        shaderProgramID     = SampleUtils::createProgramFromBuffer(_cubeMeshVertexShader,
//                                                                   _cubeFragmentShader);
        shaderProgramID     = SampleUtils::createProgramFromBuffer(lightVertexShader,
                                                                   lightFragmentShader);
        
        //for lighting shade
        mMVMatrixHandle     = glGetUniformLocation(shaderProgramID, "u_MVMatrix");
        mLightPosHandle     = glGetUniformLocation(shaderProgramID, "u_LightPos");
        mcolorHandle        = glGetAttribLocation(shaderProgramID, "a_Color");
        
        vertexHandle        = glGetAttribLocation(shaderProgramID,
                                                  "vertexPosition");
        normalHandle        = glGetAttribLocation(shaderProgramID,
                                                  "vertexNormal");
        //    textureCoordHandle  = glGetAttribLocation(shaderProgramID,
        //                                                "vertexTexCoord");
        mvpMatrixHandle     = glGetUniformLocation(shaderProgramID,
                                                   "modelViewProjectionMatrix");
        texSampler2DHandle  = glGetUniformLocation(shaderProgramID,
                                                   "texSampler2D");
        
        //for point light
        mPointProgramHandle = SampleUtils::createProgramFromBuffer(pointVertexShader,
                                                                   pointFragmentShader);
        
        pointMVPMatrixHandle = glGetUniformLocation(mPointProgramHandle, "modelViewProjectionMatrix");
        pointPositionHandle  = glGetAttribLocation(mPointProgramHandle, "vertexPosition");
    
        
#endif
        
    }
    
    
    JNIEXPORT void JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargetsRenderer_updateRendering(
                                                                                    JNIEnv* env, jobject obj, jint width, jint height)
    {
        LOG("Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargetsRenderer_updateRendering");
        
        // Update screen dimensions
        screenWidth = width;
        screenHeight = height;
        
        // Reconfigure the video background
        configureVideoBackground();
    }
    
    JNIEXPORT void JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_updateScaleFactorNative(
                                                                                    JNIEnv* env, jobject obj, jfloat _scale_factor)
    {
        scale_factor = _scale_factor;
        //return scale_factor;
    }
    
    JNIEXPORT float JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_updateRotationNative(
                                                                                 JNIEnv* env, jobject obj, jfloat _x_angle, jfloat _y_angle)
    {
        x_angle = _x_angle;
        y_angle = _y_angle;
    }
    
    JNIEXPORT void JNICALL
    Java_com_qualcomm_QCARSamples_ImageTargets_ImageTargets_loadViewMatrixNative(
                                                                                 JNIEnv* env, jobject obj, jfloatArray _viewMatrix)
    {
        // Step 1: Convert
        jfloat* floatCArray = (env)->GetFloatArrayElements(_viewMatrix, NULL);
        if (floatCArray == NULL) return;
            
        //Step 2: Accept
        for (int i = 0; i<16; i++)
            viewMatrix[i] = (float)floatCArray[i];
        
    }

    
#ifdef __cplusplus
}
#endif
