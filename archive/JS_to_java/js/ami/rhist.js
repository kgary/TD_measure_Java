export function rhist(...args){
  try{
      if(args.length <1){
          throw exit;
      }
  }catch(e){
      console.error("Insufficient no of arguments");  
  }
  if(args[0]=='Axes'){
      cax = args[0];
      args = args.slice(1,args.length);
  }
  var nargs = args.length;
  var y = args[0];
  if(nargs == 1){
      var x = 10;
  }else{
      var x = args[1];
  }
  var Y = []; var count = 0;
  for(let i=0;i<y.length;i++){
      for(let j=0;j<y[0].length;j++){
          Y[count] = y[i][j];
          count++;
      }
  }
  var m = Y.length;
  var nn = Array(x+1).fill(0);
  var centers = []
  var bins = []
  var min = Math.min(...Y);
  var max = Math.max(...Y);
  var binWidth = (max-min)/x;
  bins[0] = Number.NEGATIVE_INFINITY;
  for(let i=0;i<=x;i++){
      bins[i+1] = min + binWidth*i;
  }
  for(let i=0;i<bins.length-1;i++){
      for(let j=0;j<Y.length;j++){
          if(Y[j] > bins[i] && Y[j] <= bins[i+1]){
              nn[i] = nn[i] + 1;
          }
      }
  }
  for(let i=1;i<bins.length-1;i++){
      centers[i-1] = (bins[i]+bins[i+1])/2;
      centers[i-1] = Math.round(centers[i-1]*10000)/10000;
  }
  nn[1] = nn[0]+nn[1];
  nn = nn.slice(1,bins.length);
  for(let i=0;i<nn.length;i++){
      nn[i] = nn[i]/m;
      nn[i] = Math.round(nn[i]*10000)/10000;
  }
  if(nargs == 3){
      for(let i=0;i<nn.length;i++){
          nn[i] = nn[i]/binWidth;
          nn[i] = Math.round(nn[i]*10000)/10000;
      }
  }
  return [nn,centers];
}