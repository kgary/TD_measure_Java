import {Entropy} from './Entropy.js';
function windowed_entropy_window(seq,window_size){
    var size = seq.length-window_size;
    var ent_series = Array(size);
    for(let i=0;i<size;i++){
         ent_series[i]=Entropy(seq.slice(i,i+window_size));
    } 
    var entropySeries = Array(seq.length);
    for(let i=0;i<window_size;i++){
      entropySeries[i] = Array(1).fill(0);
    }
    for(let i=window_size;i<seq.length;i++){
      entropySeries[i] = Array(1).fill(0);
      entropySeries[i][0] = ent_series[i-window_size][0];
    }
    return entropySeries;
}
                         
console.log(windowed_entropy_window([[1],[1],[0],[0],[0],[1],[1],[1],[1],[1],[1]],4));