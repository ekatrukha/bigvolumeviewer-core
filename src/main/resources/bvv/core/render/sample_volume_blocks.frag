#define NUM_BLOCK_SCALES 10

uniform mat4 im;
uniform vec3 sourcemin;
uniform vec3 sourcemax;

void intersectBoundingBox( vec4 wfront, vec4 wback, out float tnear, out float tfar )
{
	vec4 mfront = im * wfront;
	vec4 mback = im * wback;
	intersectBox( mfront.xyz, (mback - mfront).xyz, sourcemin - 0.5, sourcemax + 0.5, tnear, tfar );
}

uniform sampler3D volumeCache;

// -- comes from CacheSpec -----
uniform vec3 blockSize;
uniform vec3 paddedBlockSize;
uniform vec3 cachePadOffset;

// -- comes from TextureCache --
uniform vec3 cacheSize;// TODO: get from texture!?


uniform usampler3D lutSampler;
uniform vec3 blockScales[ NUM_BLOCK_SCALES ];
uniform vec3 lutSize;
uniform vec3 lutOffset;

float sampleVolume( vec4 wpos )
{
	vec3 pos = (im * wpos).xyz;
	// Clamp tile lookup to minimum tile 0 so we don't hit the zero-LUT pad
	vec3 qPos = max( vec3( 0.0 ), pos );
	vec3 tileIndex = floor( qPos / blockSize );
	vec3 q = tileIndex - lutOffset + 0.5;

	uvec4 lutv = texture( lutSampler, q / lutSize );
	vec3 B0 = lutv.xyz * paddedBlockSize + cachePadOffset;
	vec3 sj = blockScales[ lutv.w ];
	
	vec3 relativePos = pos - tileIndex * blockSize;
	vec3 c0 = B0 + relativePos * sj + 0.5 * sj;

	return texture( volumeCache, c0 / cacheSize ).r;
}

