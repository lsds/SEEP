import math, pandas as pd

def compute_stats_dict(vals):
    (meanVal, stdDevVal, maxVal, minVal, medianVal, lowerQuartileVal,
            upperQuartileVal) = compute_stats(vals)

    return {'MEAN':meanVal, 'STDDEV':stdDevVal, 'MAX':maxVal, 'MIN':minVal,
            'MEDIAN':medianVal, 'LQ':lowerQuartileVal, 'UQ':upperQuartileVal}

def compute_stats(vals):

    if len(vals) == 0: return (0.0,0.0,0.0,0.0,0.0,0.0,0.0)
    meanVal = sum(vals)/len(vals)
    varianceVal = (reduce(lambda accum, x: math.pow(meanVal - x,2) + accum,
        vals, 0)) / max(1, len(vals) - 1)
    stdDevVal = math.sqrt(varianceVal) 
    sampleStdErr = stdDevVal / math.sqrt(len(vals)) 

    maxVal = max(vals)
    minVal = min(vals)
    vals.sort()
    medianVal = median(vals)
    (lowerQuartileVal,upperQuartileVal) = quartiles(vals)

    #return (meanVal, stdDevVal, maxVal, minVal, medianVal, lowerQuartileVal, upperQuartileVal)
    return (meanVal, sampleStdErr, maxVal, minVal, medianVal, lowerQuartileVal, upperQuartileVal)

def median(nums):
    if len(nums) % 2 == 1:
        return nums[len(nums)/2]
    else:
        return (nums[len(nums)/2] + nums[(len(nums)/2)-1])/2

def quartiles(nums):
    # See Method 3 @ http://en.wikipedia.org/wiki/Quartile
    if len(nums) < 4: return median(nums), median(nums)   # TODO Is this right?
    if len(nums) % 2 == 0:
        lowerQuartile = median(nums[0:len(nums)/2])
        upperQuartile = median(nums[len(nums)/2:len(nums)]) 
        return lowerQuartile,upperQuartile
    else: 
        n = len(nums)/4
        if len(nums) % 4 == 1:
            lowerQuartile = (nums[n-1]/4) + (3 * (nums[n]/4))
            upperQuartile = (3 * (nums[3*n]/4)) + (nums[(3*n)+1]/4)
            return lowerQuartile, upperQuartile
        else: 
            assert len(nums) % 4 == 3
            lowerQuartile = (3 * (nums[n]/4)) + (nums[n+1]/4)
            upperQuartile = (nums[(3*n)+1]/4) + (3 * (nums[(3*n)+2])/4) 
            return lowerQuartile, upperQuartile
         
def compute_cumulative_percentiles(nums):

    sorted_nums = sorted(nums)
    result = []

    for i, num in enumerate(sorted_nums, 1):
       result.append(((100*i)/len(sorted_nums), num))

    return result

def compute_mean_parallel_ratios(ratios):
	result = {}
	for run_ratios in ratios:
		for count in run_ratios:
			result[count] = result.get(count,0.0) + run_ratios[count] 

	for count in result:
		result[count] = result[count] / len(ratios)

	print result

	return result

def compute_relative_raw_vals(abs_vals):
	rel_vals = {}
	print 'abs_vals',abs_vals
	for k in abs_vals:
		rel_vals[k] = {}
		for mob in abs_vals[k]:
			rel_vals[k][mob]={}
			for rand in abs_vals[k][mob]:
				if float(abs_vals[1][mob][rand]) <= 0.0 : return None
				rel_vals[k][mob][rand] = float(abs_vals[k][mob][rand]) / float(abs_vals[1][mob][rand])
	print 'rel_vals',rel_vals
	return rel_vals


def compute_percentile_stats(raw_vals):
    result = {}
    val_series = pd.Series(raw_vals)
    percentiles = [0.05,.25,.50,.75,.9,.95,.99]
    result = dict(val_series.describe(percentiles=percentiles))
    print result
    result_vals = [result['mean'], result['min'], result['5%'], result['25%'], result['50%'], result['75%'], result['90%'], result['95%'], result['99%'], result['max']]
    return result_vals

