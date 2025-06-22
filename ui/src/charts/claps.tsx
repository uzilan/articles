import type Props from "../models/props.ts";
import {CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis} from "recharts";
import {DateTime} from "luxon";

export default function Claps(props: Props) {

    const data = props.articles.map(article => ({
        date: article.published,
        title: article.title,
        claps: article.claps,
        responses: article.responses,
    }))

    return (
        <>
            <h2 style={{margin: '2rem'}}>Claps and responses</h2>
            <ResponsiveContainer width="100%" height={300}>
                <LineChart data={data}>
                    <CartesianGrid strokeDasharray="3 3"/>
                    <XAxis dataKey="date"
                           angle={-45}
                           textAnchor="end"
                           dy={2}

                           height={50}
                           tickFormatter={date =>
                               DateTime.fromSQL(date).toFormat('dd MMM yyyy')
                           }
                           tick={{
                               fontSize: 8,
                           }}/>
                    <YAxis yAxisId="left" label={{value: 'Claps', angle: -90, position: 'insideLeft'}}/>
                    <YAxis yAxisId="right" orientation="right"
                           label={{value: 'Responses', angle: -90, position: 'insideRight'}}/>
                    <Tooltip
                        formatter={(value, name, payload) => {
                            if (name === 'claps') {
                                return [
                                    <>
                                        <strong style={{color: "#cb2b2b"}}>{payload.payload.title}</strong>
                                        <br/>
                                        <br/>
                                        <strong>{name}</strong>: {value}
                                    </>,
                                ];
                            } else {
                                return [
                                    <>
                                        <strong>{name}</strong>: {value}
                                    </>,
                                ];
                            }
                        }}
                    />
                    <Legend verticalAlign="top" align="center"/>
                    <Line yAxisId="left" type="monotone" dataKey="claps" stroke="#8884d8"/>
                    <Line yAxisId="right" type="monotone" dataKey="responses" stroke="#82ca9d"/>
                </LineChart>
            </ResponsiveContainer>
        </>)
}
