"""
This script is used to plot the data from the file "WindowSize.csv".
To use this script, make sure you installed pandas and plotly :
- pip install pandas
- pip install plotly
Make sure that the first line of the CSV file is like -> Time(s),WindowSize(Packet)
"""
import pandas as pd
import plotly.express as px

df = pd.read_csv('WindowSize.csv')

fig = px.line(df, x = 'Time(s)', y = 'WindowSize(Packet)', title='Size of the window as a function of time')
fig.show()