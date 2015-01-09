#ifndef GPU_DEBUG_H_
#define GPU_DEBUG_H_

#undef GPU_VERBOSE

#undef dbg
#ifdef GPU_VERBOSE
	define dbg(fmt, args...) fprintf(stdout, fmt, ## args)
#else
#	define dbg(fmt, args...)
#endif

#endif /* LOG_H_ */
