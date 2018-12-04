//
// Created by Mohammad Hoque on 26/12/2016.
//

#ifndef SENSEBATTERY_PCA_H
#define SENSEBATTERY_PCA_H



void covcol(float **data, float **symmat, int n, int m);
void corcol(float **data, float **symmat, int n, int m);
void scpcol(float **data, float **symmat, int n, int m);

extern "C" {
void erhand(char err_msg[]);
float *vector(int n);
float **matrix(int n, int m);
void free_vector(float *v, int n);
void free_matrix(float **mat, int n, int m);
void tred2(float **a, int n, float d[], float e[]);
void tqli(float d[], float e[], int n, float **z);
}

#endif //SENSEBATTERY_PCA_H
