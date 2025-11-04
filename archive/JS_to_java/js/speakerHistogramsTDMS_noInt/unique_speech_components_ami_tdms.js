import {readFile} from './createBitfieldVector.js';
import {speakerHistogramsTDMS_noInt} from './speakerHistogramsTDMS_noInt.js';
import {ami} from '/Users/satyamshekhar/Desktop/JS_to_java/js/ami/ami.js';
function unique_speech_components_ami_noInt(T1, ws){
    var num_channels = T1[0].length;
    var speech_series_length = T1.length;
    var T2 = T1;
    for(let i=0;i<num_channels;i++){
        for(let j=0;j<speech_series_length;j++){
            if(T1[j][i] == 1)
            {
              T2[j][i] = i+1;
            }
        }
    }
    var comp_series = T2;
    var ami_series = Array(T1.length-ws/2);
    for(let i=0;i<T1.length-ws/2;i++){
        ami_series[i] = Array(num_channels).fill(0); 
    }
    for(let i=0;i<num_channels;i++){
        for(let j=0;j<num_channels;j++){
            if(i!=j){
               var xy = Array(speech_series_length);
               for(let k=0;k<speech_series_length;k++){
                xy[k]=Array(2).fill(0);
                xy[k][0] = comp_series[k][i];
                xy[k][1] = comp_series[k][j];
               }
               for(let l=ws/2;l<speech_series_length-ws/2;l++){
                var z = l-ws/2
                var p = l+ws/2
                var [amis, tilt] = ami(xy.slice(l-ws/2,l+ws/2+1),[[10]],0);
                ami_series[l][i] = ami_series[l][i]+amis.reduce((a, b) => a + b, 0)/amis.length;
                ami_series[l][i] = Math.round(ami_series[l][i]*10000)/10000;
               } 
            }
        }
    }
    var tableAMI = Array(num_channels);
    for(let i=0;i<num_channels;i++){
        tableAMI[i]=Array(2);
        tableAMI[i][0] = "Channel"+(i+1);
        var eachColumn = ami_series.map(d => d[i]);
        tableAMI[i][1] = eachColumn.reduce((a, b) => a + b, 0)/eachColumn.length;
        tableAMI[i][1] = Math.round(tableAMI[i][1]*10000)/10000;
    }
    console.log(ami_series);
    return [ami_series,tableAMI];
}
var T = readFile("BinaryMatrix5911_v2_columns.xlsx");
var [T1,T2] = speakerHistogramsTDMS_noInt(T,1,T.length);
unique_speech_components_ami_noInt(T1,10);