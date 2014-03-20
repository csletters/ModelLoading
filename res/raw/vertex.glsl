uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;
uniform mat4 mv;
attribute vec2 aTexCord;
attribute vec4 aPosition;
varying vec3 vPosition;
varying vec2 vTexCord;
void main() {
vTexCord = aTexCord;
gl_Position = projection*mv*aPosition;
}