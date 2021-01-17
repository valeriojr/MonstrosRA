precision mediump float;
varying vec3 normalInterp;// Surface normal
varying vec3 vertPos;// Vertex position
varying vec2 texCoord;

uniform vec3 lightPos;
uniform vec3 cameraPosition;
uniform vec4 objectColor;
uniform sampler2D uTexture;

void main() {
    vec3 N = normalize(normalInterp);
    vec3 L = normalize(lightPos - vertPos);
    float lambertian = max(dot(N, L), 0.0);

    vec4 objectColr = texture2D(uTexture, texCoord);

    vec4 ambient = vec4(0.2) * objectColr;
    vec4 diffuse = lambertian * objectColr;
    vec4 specular = vec4(0.0);

    if(lambertian > 0.0){
        vec3 cameraDirection = normalize(cameraPosition - vertPos);
        vec3 R = reflect(-L, N);

        float spec = pow(max(dot(cameraDirection, R), 0.0), 32.0);
        specular = vec4(spec, spec, spec, 1.0);
    }

    vec4 result = ambient + diffuse + specular;
    result.w = 1.0;
    gl_FragColor = result;
}
