import { createRequire } from "module";
const require = createRequire(import.meta.url);

export function readFile(fileName){
    const reader = require('xlsx');
    const file = reader.readFile(fileName);
    let data = []
    const sheets = file.SheetNames
    for(let i = 0; i < sheets.length; i++)
    {
        const temp = reader.utils.sheet_to_json(
                file.Sheets[file.SheetNames[i]])
        temp.forEach((res) => {
            data.push(res)
        })
    }
    return data;
}

export function createBitfieldVector(file_name){
    var data = readFile(file_name);
    var nrow = Object.keys(data).length;
    var ncol = Object.keys(data[0]).length;
    var num_channels = 12;
    var vec = [];
    var bitfields = [];
    var labels = Object.keys(data[0]);
    for(let i=0;i<12;i++){
        vec[i] = 2**i;
        bitfields[i] = vec[i].toString(2);
        if(bitfields[i].length < 12){
            let s = "";
            for(let j=0;j<12-bitfields[i].length;j++){
                s = s+"0";
            }
            bitfields[i] = s + bitfields[i];
        }
    }
    var bitfield_vec = Array(nrow).fill(0);
    var labels_vec = Array(nrow).fill(0);
    for(let i=0; i<nrow;i++){
       let t = Object.values(data[i]); 
       let ind = [];
       let b0 = [];
       let count =0;
       for(let j=0;j<t.length;j++){
        if(t[j]!=0){
            ind[count] = j;
            b0[count] = bitfields[j];
            count++;
        }
       }
       //console.log(b0);
       let binary_Value = 0;
       let label = "";
       for(let j=0;j<ind.length;j++){
        binary_Value = binary_Value + parseFloat(b0[j]);
        label = label + labels[ind[j]];
        if(j!=ind.length-1){
            label = label +" + ";
        }
       }
       bitfield_vec[i] = binary_Value;
       labels_vec[i] = label;
    }
    //console.log(bitfield_vec);
    //console.log(labels_vec);
    return [bitfield_vec,labels_vec];
}
//createBitfieldVector("BinaryMatrix5911_v2_columns.xlsx");