# Selective Repeat & Congestion control
***
Implementation of the pipelining protocol *Selective Repeat* with a congestion control similar 
to the one used in *TCP reno*.<br>
Second-year project for the network course at the University of Mons.

## Simulator
***
Copyright (c) 2011 Bruno Quoitin.
All rights reserved.

## Build & Run
***

#### How to compile :
In the *src* package :
```commandline
javac -d build reso/examples/selectiveRepeat/Demo.java
```
#### How to run :
In the *build* directory created before :
```commandline
java reso.examples.selectiveRepeat.Demo
```

## Plot of the window size
***
### Dependencies
There are 2 python script to plot the size of the window.<br>
The first one uses Plotly and the secondOne uses Matplotlib.
```commandline
pip install pandas
pip install plotly
pip install matplotlib
```

### Run
After running the simulation, a file named *WindowSize.csv* will be created.
You just have to run one the python script to plot the result.
```commandline
python plot.py
python plot2.py
```

# Collaborators
***
- Arnaud MOREAU
- Cyril MOREAU
