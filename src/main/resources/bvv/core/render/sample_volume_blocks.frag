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
	
	ivec3 localQ = ivec3( tileIndex - lutOffset );
	
	if ( any( lessThan( localQ, ivec3( 0 ) ) ) || any( greaterThanEqual( localQ, ivec3( lutSize ) ) ) )
	    return 0.0;
	    
	// normalized sampling coordinate [0.0, 1.0] for lutSampler
	vec3 q = (localQ + 0.5) / lutSize;

	uvec4 lutv = texture( lutSampler, q );
	vec3 B0 = vec3(lutv.xyz) * paddedBlockSize + cachePadOffset;
	vec3 sj = blockScales[ lutv.w ];
	
	vec3 tileIndexCoarse = floor( qPos / ( blockSize * sj ) );
	vec3 relativePos = pos - tileIndexCoarse * (blockSize * sj);
	vec3 c0 = B0 + relativePos * sj + 0.5;

	return texture( volumeCache, c0 / cacheSize ).r;
}

