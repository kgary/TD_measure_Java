import {DiscreteRecurrence} from './DiscreteRecurrence.js'
function windowed_recurrence_forward_window(x, window_size){
    let sizeOfColumn = x[0].length-window_size;
    let rr_series = Array(1)
    rr_series[0]= Array(sizeOfColumn).fill(0);
    let det_series = Array(1);
    det_series[0]= Array(sizeOfColumn).fill(0);
    for(let i=0;i<sizeOfColumn;i++){
       let sequence = Array(1);
       sequence[0] = Array(window_size).fill(0);
       for(let j=0;j<window_size;j++){
            sequence[0][j]=x[0][i+j];
       }
       let [rr, det] = DiscreteRecurrence(sequence);
       
      if(rr == 0){
        rr_series[0][i] = 100;
      }else{
        rr_series[0][i] = rr;
      }
      if(det == 0){
        det_series[0][i] = 100;
      }else{
        det_series[0][i] = det;
      }
    }
    let outputLength = x[0].length
    let rr_series_final = Array(outputLength);
    let det_series_final = Array(outputLength);
    for(let i=0;i<outputLength;i++){
        rr_series_final[i]=Array(1).fill(100);
        det_series_final[i]=Array(1).fill(100);
    }
    

    for(let i=window_size;i<outputLength;i++){
        rr_series_final[i][0] =  rr_series[0][i-window_size];
        det_series_final[i][0] =  det_series[0][i-window_size];

    }
    console.log(rr_series_final);
    console.log(det_series_final);
}

windowed_recurrence_forward_window([[1,1,2,3,4,5,6,7,8,9]],4);