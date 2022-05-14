import pandas as pd
import plotly.express as px

df = pd.read_csv('WindowSize.csv')

fig = px.line(df, x = 'Time', y = 'WindowSize', title='Taille de la fenêtre en fonction du temps')
fig.show()