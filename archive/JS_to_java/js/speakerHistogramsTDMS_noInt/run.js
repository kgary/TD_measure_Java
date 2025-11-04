import { createBitfieldVector } from './createBitfieldVector.js';

const [bitfields, labels] = createBitfieldVector("BinaryMatrix5911_v2_columns.xlsx");

console.log("Bitfields:");
console.log(bitfields);

console.log("Labels:");
console.log(labels);
