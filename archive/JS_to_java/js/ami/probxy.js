function computeEdge(...args){
    try{
        if(args.length <1 || args.length > 2){
            throw exit;
        }
    }catch(e){
        if(args.length<1){
            console.error("Insufficient no of arguments"); 
        }else{
            console.error("Too many no of arguments"); 
        }   
    }
    var x = args[0];
    if(args.length == 1){
        var nBins = 10;
    }
    else{
        var nBins = args[1];
    }
    var minX = Math.min(...x);
    var maxX = Math.max(...x);
    var binwidth = (maxX-minX)/nBins;
    var edge = []
    for(let i=1;i<nBins;i++){
        edge[i] = (minX+ binwidth*i)*10000/10000;
    }
    edge[0] = Number.NEGATIVE_INFINITY;
    edge[nBins] = Number.POSITIVE_INFINITY;
    return edge;
}
function transpose(matrix) {
    return matrix[0].map((col, i) => matrix.map(row => row[i]));
}
export function probxy(...args){
    try{
        if(args.length<1 || args.length > 3){
            throw exit;
        }
    }catch(e){
        if(args.length<1){
            console.error("Insufficient no of arguments"); 
        }else{
            console.error("Too many no of arguments"); 
        }   
    }
    var xy = args[0];
    var m = xy.length;
    var n = xy[0].length;
    if(n > m){
        xy = transpose(xy);
        m = xy.length;
        n = xy[0].length;
    }
    try{
        if(n != 2){
            throw exit;
        }
        else{
            var X = xy.map(d => d[0]);
            var Y = xy.map(d => d[1]);
        }
    }catch(e){
        console.error("Invalid data size: XY should be two column vector"); 
    }
    if(args.length - 1 ==0){
        var nBinsX = 10;
        var nBinsY = 10;
    }else if(args.length - 2 ==0){
        var nBinsX = args[1];  
        var nBinsY = 10;
    }else{
        var nBinsX = args[1];  
        var nBinsY = args[2];   
    }
    if(!Array.isArray(nBinsX) && nBinsX>0){
        var edgeX = computeEdge(X,nBinsX);
    }else if((nBinsX.length == 1 && nBinsX[0].length == 1) && nBinsX[0][0]>0){
       var edgeX = computeEdge(X,nBinsX[0][0]);
    }
    else if(nBinsX.length == 1|| nBinsX[0].length == 1){
        var edgeX = [];
        var count = 0;
        for(let i=0;i<nBinsX[0].length;i++){
            for(let j=0;j<nBinsX.length;j++){
                edgeX[count] = nBinsX[j][i];
                count++;
            }
        }
        nBinsX = edgeX.length - 1;
    }
    var edgeY;
    if(!Array.isArray(nBinsY) && nBinsY>0){
        edgeY = computeEdge(Y,nBinsY);
    }else if((nBinsY.length == 1 && nBinsY[0].length == 1) && nBinsY[0][0]>0){
        edgeY = computeEdge(Y,nBinsY[0][0]);
    }else if(nBinsY.length == 1 || nBinsY[0].length == 1){
        edgeY = [];
        count = 0;
        for(let i=0;i<nBinsY[0].length;i++){
            for(let j=0;j<nBinsY.length;j++){
                edgeY[count] = nBinsY[j][i];
                count++;
            }
        }
        nBinsY = edgeY.length - 1;
    }
    var nn = Array(nBinsX);
    for(let i=0; i<nBinsX; i++){
        nn[i] = Array(nBinsY).fill(0);
    }
    for(let i=0;i<nBinsX;i++){
        var yFound = []
        var k=0;
        for(let j=0;j<X.length;j++){
            if(X[j]>=edgeX[i] && X[j]<edgeX[i+1]){
                yFound[k] = Y[j];
                k++;
            }
        }
        var n = Array(edgeY.length).fill(0);
        if(yFound.length!=0){
            for(let j=0;j<yFound.length;j++){
                for(let p=0;p<edgeY.length-1;p++){
                    if(yFound[j]>=edgeY[p] && yFound[j]<edgeY[p+1]){
                        n[p] = n[p]+1;
                    }
                }
            }
            n[n.length-2] = n[n.length-2]+n[n.length-1];
        }
        n = n.slice(0,n.length-1);
        for(let j=0;j<nBinsY;j++){
            nn[i][j] = n[j];
        }
    }
    var pxy = Array(nn.length);
    for(let i=0;i<nn.length;i++){
        pxy[i] = Array(nn[0].length).fill(0);
        for(let j=0;j<nn[0].length;j++){
            pxy[i][j] = nn[i][j]/X.length;
            pxy[i][j] = Math.round(pxy[i][j]*10000)/10000;
        }
    }
    return pxy;
}
//console.log(probxy([[1,2],[1,2],[1,2]],2,3));
//console.log(probxy([[2,4],[3,5],[5,6]],[[1,2,3]],[[4,5,6]]));