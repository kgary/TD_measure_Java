import {readFile} from './createBitfieldVector.js';
export function speakerHistogramsTDMS_noInt(T, timeOn, timeOff){
    var nrow = Object.keys(T).length;
    var ncol = Object.keys(T[0]).length;
    var channels = Object.keys(T[0]);
    var a = Object.values(channels[0]);
    var channelCount = Array(ncol).fill(0);
    var size;
    if(timeOff < nrow){
        size = timeOff;
    }
    else{
        size = nrow;
    }
    let k = 0;
    var T1 = Array(size - timeOn + 1);
    for(let i=timeOn-1;i<size;i++){
        var t = Object.values(T[i]);
        T1[k] = Array(ncol).fill(0);
        for(let j=0;j<t.length;j++){
            T1[k][j] = t[j];
            if(t[j] == 1){
                channelCount[j] = channelCount[j] + 1;
            }
        }
        k = k+1;
    }

    var T2 = Array(ncol);
    for(let i=0;i<ncol;i++){
        T2[i] = Array(2);
        T2[i][0] = channels[i];
        T2[i][1] = channelCount[i];
    }
    T2.sort(function(a,b){
        return b[1]-a[1];
    });
    //console.log(T2);
    return [T1,T2];
}
//var T = readFile("BinaryMatrix5911_v2_columns.xlsx");
//speakerHistogramsTDMS_noInt(T,1,T.length);