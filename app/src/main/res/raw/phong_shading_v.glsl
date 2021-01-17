attribute vec3 aPosition;
attribute vec3 aNormal;
attribute vec2 aTexCoord;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

varying vec3 normalInterp;
varying vec3 vertPos;
varying vec2 texCoord;

void main(){
    vec4 vertPos4 = uModel * vec4(aPosition, 1.0);
    vertPos = vec3(vertPos4);
    normalInterp = (uModel * vec4(aNormal, 1)).xyz;
    texCoord = aTexCoord;

    gl_Position = uProjection * uView * vertPos4;
}
