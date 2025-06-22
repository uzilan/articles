import type Props from "../models/props.ts";
import {Cell, Legend, Pie, PieChart, Tooltip} from "recharts";

export default function Publications(props: Props) {

    const grouped = props.articles.reduce<Record<string, number>>((acc, article) => {
        const publication = article.publication || '-';
        acc[publication] = (acc[publication] || 0) + 1;
        return acc;
    }, {})

    const data = Object.entries(grouped).map((group, _) =>
        ({value: group[1], name: group[0]}))
        .sort((p1, p2) => p2.value - p1.value)

    const getColor = (index: number, total: number) => {
        const hue = (index * 360) / total
        return `hsl(${hue}, 60%, 50%)`
    }

    function CustomLegend({payload}: any) {
        return (
            <div style={{
                maxHeight: 400, overflowY: 'scroll', paddingLeft: 16, scrollbarWidth: 'thin',
                scrollbarColor: '#888 #eee',
            }} className="custom-scrollbar">
                {payload.map((entry: any, index: number) => (
                        <div key={`item-${index}`} style={{display: 'flex', alignItems: 'center', marginBottom: 4}}>
                            <div
                                style={{
                                    width: 10,
                                    height: 10,
                                    backgroundColor: entry.color,
                                    marginRight: 8,
                                }}
                            />
                            <span style={{color: '#000'}}>{entry.value}</span>
                        </div>
                    )
                )
                }
            </div>
        )
    }

    return (
        <>
            <h2 style={{margin: '2rem'}}>Publications</h2>
            <PieChart width={1000} height={500} style={{marginBottom: '5rem'}}>
                <Pie data={data} dataKey="value" nameKey="name" cx="60%" cy="50%" outerRadius={200}>
                    {data.map((_, index) => (
                        <Cell
                            key={`cell-${index}`}
                            fill={getColor(index, data.length)}
                        />
                    ))}
                </Pie>
                <Tooltip/>
                <Legend
                    content={<CustomLegend/>}
                    layout="vertical"
                    align="right"
                    verticalAlign="middle"
                />
            </PieChart>
        </>
    );
}
