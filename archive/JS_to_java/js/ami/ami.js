import {probxy} from './probxy.js';
import {prob} from './prob.js';

function corrcoef(X, Y, n)
{
      
    let sum_X = 0, sum_Y = 0, sum_XY = 0;
    let squareSum_X = 0, squareSum_Y = 0;
     
    for(let i = 0; i < n; i++)
    {
          
        sum_X = sum_X + X[i];
     
        sum_Y = sum_Y + Y[i];
     
        sum_XY = sum_XY + X[i] * Y[i];
     
        squareSum_X = squareSum_X + X[i] * X[i];
        squareSum_Y = squareSum_Y + Y[i] * Y[i];
    }
     
    let corr = (n * sum_XY - sum_X * sum_Y)/
               (Math.sqrt((n * squareSum_X -
                       sum_X * sum_X) * 
                          (n * squareSum_Y - 
                       sum_Y * sum_Y)));
     
    return corr;
}

function transpose(matrix) {
    return matrix[0].map((col, i) => matrix.map(row => row[i]));
  }
export function ami(xy,nBins,nLags){
    try{
        if(arguments.length > 3 || arguments.length < 3){
            throw exit;
        }
    }catch(e){
        if(arguments.length < 3){
            console.error("Insufficient no of arguments"); 
        }else{
            console.error("Too many no of arguments"); 
        }   
    }
    var m = xy.length;
    var n = xy[0].length;
    if(n > m){
        xy = transpose(xy); 
        let temp = n;
        n = m;
        m = temp;
    }
    try{
        if(n > 2){
            throw exit;
        }
        else if(n == 2){
            var x = xy.map(d => d[0]);
            var y = xy.map(d => d[1]);
        }else if(m == 1 | n == 1){
            let temp = transpose(xy);
            var y = temp[0];
            var x = temp[0];
        }
    }catch(e){
        console.error("Invalid time series data: Time series should be univariate or bivariate");
    }
    var nBinsRowSize = nBins.length;
    var nBinsColSize = nBins[0].length;
    if(nBinsRowSize < nBinsColSize){
        nBins = transpose(nBins);
        nBinsRowSize = nBins.length;
        nBinsColSize = nBins[0].length;
    }
    try{
        if(nBinsRowSize > 2 || nBinsColSize > 1){
            throw exit;
        }else if(nBinsRowSize == 2 && n == 2){
            var xBin = Math.floor(nBins[0]);
            var yBin = Math.floor(nBins[1]);
        }else if((nBinsRowSize == 1 && n == 2) || n == 1){
            var xBin = Math.floor(nBins[0]);
            var yBin = xBin;
        }else{
            throw exit;
        }
    }catch(e){
        console.error("Invalid bin size: It should be either vector of 2 elements or scalar");
    }
    try{
        if(nLags < 0){
            throw exit;
        }
        if(nLags > m){
            throw exit;
        }
        
    }catch(e){
        if(nLags < 0){
            console.error("Invalid lag: It should be a positive scalar");       
        }
        if(nLags > m){
            console.error("Invalid lag: It should not be greater than length of time series data");  
        }
    }
    nLags = Math.floor(nLags);
    var amis = Array(1);
    amis[0] = Array(nLags+1).fill(0);
    var corrs = Array(1);
    corrs[0] = Array(nLags+1).fill(0);
    for(let i=0;i<nLags+1;i++){
        var xlag = x.slice(0,x.length-i);
        var ylag = y.slice(i,x.length);
        var px, xBinComputed, py, yBinComputed, pxy;
        [px , xBinComputed] = prob([xlag],xBin);
        [py , yBinComputed] = prob([ylag],yBin);
        var ab = Array(xlag.length);
        for(let j=0;j<xlag.length;j++){
            ab[j] = Array(2).fill(0);
            ab[j][0] = xlag[j];
            ab[j][1] = ylag[j];
        }
        pxy = probxy(ab,xBinComputed,yBinComputed);
        var amixy = 0;
        for(let j=0;j<xBinComputed;j++){
            for(let k = 0;k<yBinComputed;k++){
                if(pxy[j][k]!=0){
                    amixy = amixy + (pxy[j][k]*Math.log2(pxy[j][k]/(px[j]*py[k])));
                    amixy = Math.round(amixy*10000)/10000;
                }
            }
        }
        amis[0][i] = amixy; 
        corrs[0][i] = Math.round(corrcoef(xlag,ylag,xlag.length)*10000)/10000;
    }
    return [transpose(amis), transpose(corrs)];   
}
// console.log([[1,2,4].concat([3,5,6])]);

//console.log(ami([[11,69],[54,74],[98,8],[19,18],[29,90]],[[5,2]],3));