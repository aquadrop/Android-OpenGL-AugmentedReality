#ifndef _QCAR_LIGHT_SHADERS_H_
#define _QCAR_LIGHT_SHADERS_H_

#ifndef USE_OPENGL_ES_1_1

//referenced from https://github.com/learnopengles/Learn-OpenGLES-Tutorials/blob/master/android/AndroidOpenGLESLessons/src/com/learnopengles/android/lesson2/LessonTwoRenderer.java

static const char* lightVertexShader = " \
\
attribute vec4 vertexPosition; \
attribute vec4 vertexNormal; \
attribute vec2 vertexTexCoord; \
\
\
uniform mat4 modelViewProjectionMatrix; \
\
\
uniform mat4 u_MVMatrix; \
uniform vec3 u_LightPos; \
attribute vec4 a_Color; \
\
varying vec2 texCoord; \
varying vec4 v_Color; \
void main() \
{ \
vec3 modelViewVertex = vec3(u_MVMatrix * vertexPosition); \
vec3 modelViewNormal = vec3(u_MVMatrix * vertexNormal); \
float distance = length(u_LightPos - modelViewVertex); \
vec3 lightVector = normalize(u_LightPos - modelViewVertex); \
float diffuse = max(dot(modelViewNormal, lightVector), 0.1); \
diffuse = diffuse * (1.0 / (1.0 + (0.007 * distance * distance))); \
v_Color = a_Color * diffuse; \
gl_Position = modelViewProjectionMatrix * vertexPosition; \
texCoord = vertexTexCoord; \
} \
";


static const char* lightFragmentShader = " \
\
precision mediump float; \
\
varying vec2 texCoord; \
varying vec4 v_Color; \
\
uniform sampler2D texSampler2D; \
\
void main() \
{ \
gl_FragColor = v_Color; \
} \
";


static const char* perVertexShader = "\
uniform mat4 u_MVPMatrix;\
uniform mat4 u_MVMatrix; \
attribute vec4 vertexPosition; \
attribute vec4 a_Color;     \
attribute vec3 vertexNormal;     \
varying vec3 v_Position;      \
varying vec4 v_Color;         \
varying vec3 v_Normal;         \
\
\
void main()\
{\
    \
    v_Position = vec3(u_MVMatrix * vertexPosition);\
    \
    \
    v_Color = a_Color;\
    \
    v_Normal = vec3(u_MVMatrix * vec4(vertexNormal, 0.0));\
    \
    \
    gl_Position = u_MVPMatrix * vertexPosition;\
}\
";

static const char* perFragmentShader = "\
precision mediump float;       // Set the default precision to medium. We don't need as high of a \
// precision in the fragment shader.\
uniform vec3 u_LightPos;       // The position of the light in eye space.\
\
varying vec3 v_Position;       // Interpolated position for this fragment.\
varying vec4 v_Color;          // This is the color from the vertex shader interpolated across the\
// triangle per fragment.\
varying vec3 v_Normal;         // Interpolated normal for this fragment.\
\
// The entry point for our fragment shader.\
void main()\
{\
    // Will be used for attenuation.\
    float distance = length(u_LightPos - v_Position);\
    \
    // Get a lighting direction vector from the light to the vertex.\
    vec3 lightVector = normalize(u_LightPos - v_Position);\
    \
    // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are\
    // pointing in the same direction then it will get max illumination.\
    float diffuse = max(dot(v_Normal, lightVector), 0.1);\
    \
    // Add attenuation.\
    diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));\
    \
    // Multiply the color by the diffuse illumination level to get final output color.\
    gl_FragColor = v_Color * diffuse;\
}\
";

static const char* pointVertexShader = " \
uniform mat4 modelViewProjectionMatrix; \
attribute vec4 vertexPosition; \
void main() \
{ \
    gl_Position = modelViewProjectionMatrix * vertexPosition; \
    gl_PointSize = 5.0; \
} \
";

static const char* pointFragmentShader = " \
    precision mediump float; \
    void main() \
    { \
        gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0); \
    } \
";
#endif

#endif


