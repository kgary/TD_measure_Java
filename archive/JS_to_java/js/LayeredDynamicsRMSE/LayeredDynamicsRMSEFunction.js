function LayeredDynamicsRMSEFunction(series, window_size, forecasting_distance, zeta){
  var mat = Array(series.length - (forecasting_distance) - (window_size/2));
  var RMSE = [];
  for(let i=0;i<series.length - (forecasting_distance) - (window_size/2);i++){
      mat[i]=Array(forecasting_distance).fill(0); 
  }
  for(let h=0;h < series.length - (window_size/2);h++){
      var currPt = series[h]; 
      for(let i=(window_size/2); i<series.length - (forecasting_distance+1);i++){
          if(!(series[i]<(currPt-zeta) || series[i]>(currPt+zeta))){
             var nn = series.slice(i,(i+forecasting_distance));
             mat[i] = Array(nn.length);
             for(let j=0;j<nn.length;j++){
              mat[i][j]=nn[j];
             }
          }          
          if(i == series.length - (forecasting_distance+1)-1){
              var matMean = [];
              for(let j=0;j<forecasting_distance;j++){
                  var matcol = [];
                  var count = 0;
                  for(let k=0;k<series.length - (forecasting_distance) - (window_size/2);k++){
                      if(mat[k][j] !=0){
                          matcol[count] = mat[k][j];
                          count++;
                      }
                  }
                  var sum = 0;
                  for(let k=0;k<matcol.length;k++){
                      sum = sum + matcol[k];
                  }
                  matMean[j] = sum*1.0/matcol.length;
              }
              var currN = series.slice(h,h+forecasting_distance);
              var sum = 0;
              for(let k = 0; k<forecasting_distance; k++){
                  sum = sum + Math.sqrt((currN[k]-matMean[k])**2);
              }
              RMSE[h]=sum*1.0/forecasting_distance;
              RMSE[h] = Math.round(RMSE[h]*10000)/10000;
          }
      }
      
  }
  //console.log(RMSE);
  return RMSE;
}

console.log(LayeredDynamicsRMSEFunction([1,2,3,4,5,6,3,4,5],2,2,2));