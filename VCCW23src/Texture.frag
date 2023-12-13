#version 330 core

in vec4 color;
in vec2 TexCoord;

out vec4 fragColor;

uniform sampler2D tex1;

void main()
{
    vec2 flippedTexCoord = vec2(TexCoord.x, 1.0 - TexCoord.y);

    vec4 texColor = texture(tex1, flippedTexCoord);

    fragColor = color * texColor ;
}
