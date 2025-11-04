function sparseDiagonals(grid){
    var nrows = grid.length;
    var ncols = grid[0].length;
    let n = Math.min(nrows,ncols);
    var diagonals=[[]];
    var row = 0;
    var col = 1;
    var j=0;
    for (let k = 0; k < ncols-1; k++) {
        let diagonal = Array(n).fill(0);
        let count =0;
        for (let i = 0; i < n; ++i) {
            let currentRow = row+i;
            let currentCol = col+i;
            if(currentRow<nrows && currentCol<ncols){
                diagonal[i] = grid[currentRow][currentCol];
                if(diagonal[i] != 0){
                    count++;
                }
            }
        }
        if(count !=0){
            diagonals[j] = diagonal;
            j++;
        }
        col++
    }
    let spdiags = Array(diagonals[0].length);
    for(let i=0;i<diagonals[0].length;i++){
        spdiags[i] = Array(diagonals.length).fill(0);
        for(let j=0;j<diagonals.length;j++){
            spdiags[i][j] = diagonals[j][i];
        }
    }
    return spdiags;
}
export function DiscreteRecurrence(sequence){
    let rowSize = sequence.length;
    let columnSize = sequence[0].length;

    if(columnSize>rowSize){
    sequence = sequence[0].map((_, colIndex) => sequence.map(row => row[colIndex]));
    }

    rowSize = sequence.length;
    columnSize = sequence[0].length;

    let RecurMatrix = Array(rowSize);
    for(let i=0;i<rowSize;i++){
        RecurMatrix[i] = Array(rowSize).fill(0);
    }
    for(let i=0;i<rowSize;i++){
        for(let j=0;j<rowSize;j++){   
            if(sequence[i] - sequence[j] == 0){
                RecurMatrix[i][j] = 1;
            }       
        }
    }

    let RecurPoints = 0
    for(let i=0;i<rowSize;i++){
        let columnSum = 0
        for(let j=0;j<rowSize;j++){
        columnSum = columnSum +  RecurMatrix[j][i];
        }
        RecurPoints = RecurPoints +  columnSum-1 ;
    }
    RecurPoints = RecurPoints/2;
    let DiagPoints = 0;
    let upperTriangleMatrix = Array(rowSize);
    for(let i=0;i<rowSize;i++){
        upperTriangleMatrix[i] = Array(rowSize).fill(0);
    }
    for(let i=0;i<rowSize-1;i++){
        for(let j=i+1;j<rowSize;j++){
            upperTriangleMatrix[i][j]=RecurMatrix[i][j];
        }
    }
 
    let B = sparseDiagonals(upperTriangleMatrix);
    let r_diags = B.length;
    var c_diags = 0;
    if(r_diags!=0){
        c_diags = B[0].length;
    }
    
    for(let j=0;j<c_diags;j++){
        for(let i=0;i<r_diags-1;i++){
            if(B[i][j] == 1 && B[i+1][j] ==1 ){
                DiagPoints = DiagPoints + 2; 
                if(i-1 >=0 && B[i-1][j]==1){
                    DiagPoints = DiagPoints - 1;  
                }
            }
        }
    }

    let RR = Math.round((RecurPoints/((rowSize*columnSize)/2))*10000)/10000;
    let DET = Math.round((DiagPoints/RecurPoints)*100*10000)/10000;
    return [RR,DET];
}
//let sequence = [[1,1,1,1,0,0,1]];
//console.log(DiscreteRecurrence(sequence));