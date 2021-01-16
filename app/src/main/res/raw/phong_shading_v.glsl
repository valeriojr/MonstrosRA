attribute vec3 aPosition;
attribute vec3 aNormal;
uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

varying vec3 normalInterp;
varying vec3 vertPos;

void main(){
    vec4 vertPos4 = uModel * vec4(aPosition, 1.0);
    vertPos = vec3(vertPos4);
    normalInterp = (uModel * vec4(aNormal, 1)).xyz;
    gl_Position = uProjection * uView * vertPos4;
}
