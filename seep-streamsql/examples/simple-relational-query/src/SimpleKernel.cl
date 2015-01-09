__kernel void simpleKernel (
	const int tuples,
	const int bytes,
	__global const uchar *input,
	__global uchar *output
) {
	int id = (int) get_global_id(0);
	if (id >= tuples)
		return;
	// uchar x = input[0];
	uchar r;
	for (int j = 0; j < 20; j++)
		for (int i = 0; i < 30; i++)
			r += (i * j * i / 1024) % 125;
	output[id] = (uchar) r % 125;
	return ;
}
