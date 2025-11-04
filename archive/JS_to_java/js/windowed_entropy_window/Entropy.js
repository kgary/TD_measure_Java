export function Entropy(X){
   let rowSize = X.length;
   let columnSize = X[0].length; 
   let H = Array(columnSize).fill(0)
   for(let column=0;column<columnSize;column++){
     var Alphabet = X.map(function(value){
        return value[column];
     });
     var uniqueAlphabets = [...new Set(Alphabet)];
     var Frequency = {}
     Alphabet.forEach(element => {
        Frequency[element] = (Frequency[element] || 0)+1;
     });
     for(let i=0;i<uniqueAlphabets.length;i++){
        var probabilty = Frequency[uniqueAlphabets[i]]/Alphabet.length;
        H[column] = H[column]+ (probabilty*Math.log2(probabilty));
     }
     if (H[column] !=0){
        H[column] = -Math.round(H[column]*10000)/10000;
     }
   }
   return H;
}

//var X = [[1,1],[1,2],[1,1],[1,2],[1,1],[1,2],[1,1],[1,2],[1,1],[1,1]]
//console.log(Entropy(X));