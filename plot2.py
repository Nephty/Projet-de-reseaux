"""
This script is also used to plot windowSize.csv but it uses pyplot from matplotlib.
To use this script, make sure you installed pandas and matplotlib :
- pip install pandas
- pip install matplotlib
Make sure that the first line of the CSV file is like -> Time(s),WindowSize(Pack
"""
import matplotlib.pyplot as plt
import pandas as pd
data = pd.read_csv('WindowSize.csv')
date=data['Time(s)']
cases=data['WindowSize(Packet)']
x=list(date)
y=list(cases)
plt.plot(x, y, color = 'g', linestyle = 'dashed', marker = 'o', label = 'Size of the window as a function of time')
plt.xlabel('Time[s]')
plt.xticks(rotation = 25)
plt.ylabel('Window Size[Packet]')
plt.title('Size of the window as a function of time')
plt.legend()
plt.show()