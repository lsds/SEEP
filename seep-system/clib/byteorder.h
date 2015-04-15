#ifndef __GPU_BYTEORDER_H_
#define __GPU_BYTEORDER_H_

#define __bswap32(x) 	(\
						(((x) & 0xff000000) >> 24) | \
						(((x) & 0x00ff0000) >>  8) | \
						(((x) & 0x0000ff00) <<  8) | \
						(((x) & 0x000000ff) << 24) )

#define __bswap64(x) 	((__bswap32(x) << 32) | __bswap32((x) >> 32))

inline float __bswapfp (float _x) {
	float _y;
	char *x = (char *) &_x;
	char *y = (char *) &_y;
	y[0] = x[3];
	y[1] = x[2];
	y[2] = x[1];
	y[3] = x[0];
	return _y;
}

#endif /* __GPU_BYTEORDER_H_ */
