This directory implements the contextService. It has access to sensor
data and extracts context information and passes it to the above layers.

Parts of this engine:
=====================
a. Machine learning algorithms (SVM, Decision Tree, GMM, etc)
b. Training GUI for providing annotated training data
c. Classification GUI for specifying required contexts using sensors,
features, sampling rates, classification algorithms etc.
d. Integrate Funf into this.
e. Allow handles for adding new contexts and implementing machine
learning algorithms.
f. API for apps to access the context information from this layer
