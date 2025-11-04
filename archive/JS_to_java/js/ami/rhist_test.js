import { rhist } from './rhist.js';

function testCase(name, y, ...args) {
    try {
        const [nn, centers] = rhist(y, ...args);
        console.log(`\n=== ${name} ===`);
        console.log('nn:', nn);
        console.log('centers:', centers);
    } catch (e) {
        console.error(`Error in "${name}":`, e.message);
    }
}
//testing prob.js
//testCase('prob.js',[[1,0,0,1,1,0,0,1,1,1]],2);

// Base case
testCase('Base case', [[1, 2, 3], [4, 5, 6]], 2);

// Single bin
testCase('Single bin', [[1, 2, 3, 4, 5, 6]], 1);

// More bins than unique values
testCase('More bins than values', [[1, 1, 2, 2, 3, 3]], 10);

// Duplicate values
testCase('All same values', [[5, 5], [5, 5]], 4);

// Negative values
testCase('Negative values', [[-3, -2], [0, 1, 2]], 3);

// Values on bin edges
testCase('Edge-aligned values', [[1, 2, 3, 4]], 3);

// Normalize by bin width
testCase('Density normalization', [[1, 2, 3], [4, 5, 6]], 2, 1);

// Empty input
testCase('Empty input', [[]]);

// Invalid format (not 2D)
testCase('Invalid input (1D)', [1, 2, 3]);
