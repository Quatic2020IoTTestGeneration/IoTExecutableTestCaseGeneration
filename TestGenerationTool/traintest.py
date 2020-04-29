from sklearn.linear_model import LogisticRegression
from pandas import read_csv
from numpy import delete
from pdb import set_trace
from sys import stdin
import traceback


def traintest(model, trainfile, testfile):
    # cols=[' Mean of the integrated profile','Standard deviation of the integrated profile','Excess kurtosis of the integrated profile','Skewness of the integrated profile','Mean of the DM-SNR curve','Standard deviation of the DM-SNR curve','Excess kurtosis of the DM-SNR curve','Skewness of the DM-SNR curve','label']
    cols = ['x', 'y', 'label']
    # cols=['seplen', 'sepwith', 'petlen', 'petwidth', 'label']
    classlb = 'label'
    score = 0
    pdtrainset = read_csv(trainfile, names=cols)

    pdtestset = read_csv(testfile, names=cols)

    trainset = pdtrainset.as_matrix()
    testset = pdtestset.as_matrix()

    print('Sets loaded')

    traincls = trainset[:, len(trainset[0]) - 1]
    trainfts = delete(trainset, len(trainset[0]) - 1, 1)

    testcls = testset[:, len(testset[0]) - 1]
    testfts = delete(testset, len(testset[0]) - 1, 1)

    print('features and labels splitted')
    model.fit(trainfts, traincls)
    print('model fitted')

    currow = testfts[0].reshape(1, -1)
    pred = model.predict(currow)
    traceback.print_stack()


if __name__ == '__main__':
    model = LogisticRegression(solver='newton-cg', multi_class='multinomial')
    traintest(model,
              'C:/Users/olly1/Dropbox/Tirocinio Dario/MachineLearningTesting/datasets/bmi/new/trainnew_nobmi.csv',
              'C:/Users/olly1/Dropbox/Tirocinio Dario/MachineLearningTesting/datasets/bmi/new/testnew_nobmi.csv')
