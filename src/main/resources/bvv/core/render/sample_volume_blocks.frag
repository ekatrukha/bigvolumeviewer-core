uniform mat4 im;
uniform vec3 sourcemin;
uniform vec3 sourcemax;

void intersectBoundingBox( vec4 wfront, vec4 wback, out float tnear, out float tfar )
{
	vec4 mfront = im * wfront;
	vec4 mback = im * wback;
	intersectBox( mfront.xyz, (mback - mfront).xyz, sourcemin, sourcemax, tnear, tfar );
}


uniform vec3 blockScales[ NUM_BLOCK_SCALES ];
uniform vec3 lutOffset;
uniform float globalZlutOffset;

float sampleVolume( vec4 wpos )
{
	vec3 pos = (im * wpos).xyz + 0.5;
	vec3 q = floor( pos / cacheBlockSize ) - lutOffset + 0.5;
	q.z = q.z + globalZlutOffset;

	uvec4 lutv = texture( u_GlobalLut, q / globalLutSize );
	vec3 B0 = lutv.xyz * paddedBlockSize + cachePadOffset;
	vec3 sj = blockScales[ lutv.w ];

	vec3 c0 = (B0 + mod( pos * sj, cacheBlockSize ) + 0.5 * sj) / cacheSize;
	                                       // + 0.5 ( sj - 1 )   + 0.5 for tex coord offset
//$insert{cacheType}

}