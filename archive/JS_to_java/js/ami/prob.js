import {rhist} from './rhist.js';
function isZeroDistribution(vec,nBins){
    let [nn,x] = rhist(vec,nBins);
    let z=0;
    for(let i=0;i<nn.length;i++){
        if(nn[i] == 0){
            z++;
        }
    }
    if(z > 0){
        return true;
    }
    return false;
}
export function prob(...args){
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
    var y = args[0];
    if(args.length==1){
        var maxBins = 10;
    }else{
        var maxBins = args[1]; 
    }
    if(!Array.isArray(y)){
        console.error("Y should be a vector");  
        throw exit;
    }
    var preBin = 0;
    var isNotZeroBin = false;
    var iter = 0;
    var cBin = maxBins; 
    var zeroBin = 0;
    while(preBin != cBin){
        let zeroDistribution = isZeroDistribution(y,cBin);
        iter = iter+1;
        if (!zeroDistribution){
            if(iter == 1){
                break;
            }
            var tmpBin = cBin;
            var nonZeroBin = cBin;
            cBin = Math.floor((zeroBin+nonZeroBin)/2);
            preBin = tmpBin; 
            isNotZeroBin = true;
        }else{
            if(!isNotZeroBin){
                preBin = cBin;    
                zeroBin = cBin;
                cBin = Math.floor(cBin/2);
            }else{
                tmpBin = cBin;     
                zeroBin = cBin;   
                cBin = Math.floor((zeroBin+nonZeroBin)/2);
                preBin = tmpBin;
            }
        }
            
    }
    let nBins = cBin;
    let py = rhist(y,nBins);
    return [py[0], nBins];
}

//console.log(prob([[1,0,0,1,1,0,0,1,1,1]],3));