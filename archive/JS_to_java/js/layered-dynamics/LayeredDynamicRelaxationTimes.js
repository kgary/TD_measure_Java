import { createRequire } from "module";
const require = createRequire(import.meta.url);
function LayeredDynamicsRelaxationTimes(time1, time2, series){
    const percentile = require("percentile");
    var failureDuration = series.slice(time1,time2+1);
    var percentileValue = percentile(99,failureDuration);
    var aboveThresh_times = [];
    var crossings = [];
    let j=0;
    for(let i=0;i<failureDuration.length;i++){
        if(failureDuration[i]>=percentileValue){
            aboveThresh_times[j] = i;
            crossings[j] = failureDuration[i];
            j++;
        }
    }
    var rt_init = aboveThresh_times[0];
    var rt_peak = [];
    j = 0;
    for(let i = 0;i<failureDuration.length;i++){
        if(failureDuration[i] - Math.max(failureDuration == 0)){
            rt_peak[j] = i;
            j++;
        }
    }
    var last = aboveThresh_times[aboveThresh_times.length-1];
    var rt_last = last;
    var times = [rt_init , rt_peak[0] , rt_last]
    
    return times;
}

console.log(LayeredDynamicsRelaxationTimes(0,5,[1,2,3,3,2,1,2,1,1]));