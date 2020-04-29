class Cloud:

    def __init__(self):
        self.valuesToDiscard = 0
        self.threshold = 160
        self.numReadings = 0
        self.maxReadings = 20
        self.glucoseReadings = [0] * self.maxReadings

    def getCriticalCount(self):
        res = 0
        for x in self.glucoseReadings:
            if(x > self.threshold):
                res += 1
        return res

    def reset(self):
        self.valuesToDiscard = 0;
        self.numReadings = 0;
        self.glucoseReadings = [0] * self.maxReadings;

    def receiveOver(self):
        if(self.valuesToDiscard == 0):
            self.glucoseReadings[self.numReadings] = self.threshold+1
            self.numReadings += 1
            if(self.numReadings == 21):
                self.numReadings = 1
        else:
            self.valuesToDiscard -= 1

    def receiveUnder(self):
        if (self.valuesToDiscard == 0):
            self.glucoseReadings[self.numReadings] = self.threshold - 1
            self.numReadings += 1
            if (self.numReadings == 21):
                self.numReadings = 1
        else:
            self.valuesToDiscard -= 1

    def resetReadings(self):
        self.numReadings = 0;
        self.glucoseReadings = [0] * self.maxReadings;

    def discardNext(self, qty):
        self.valuesToDiscard = qty;

    def getThreshold(self):
        return self.threshold

    def getNumReadings(self):
        return self.numReadings

    def getMaxReadings(self):
        return self.maxReadings

