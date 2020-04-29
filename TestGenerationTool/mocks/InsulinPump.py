class InsulinPump:

    def __init__(self):
        self.erogatedInjections = 0

    def inject(self):
        self.erogatedInjections += 1

    def reset(self):
        self.erogatedInjections = 0

    def getErogatedInjections(self):
        return self.erogatedInjections