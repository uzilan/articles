import './App.css'
import Publications from "./charts/publications.tsx";
import Timeline from "./charts/timeline.tsx";
import Cloud from "./charts/cloud.tsx";
import Claps from "./charts/claps.tsx";
import Stories from "./charts/stories.tsx";
import type Data from "./models/Data.ts";

interface Props {
    data: Data
}

export default function Stats(props: Props) {
    return (
        <>
            <h1>{props.data.name}</h1>
            <div><strong>{props.data.followers}</strong></div>
            <div style={{width: '100%', textAlign: 'center'}}>
                <p style={{width: "450px", margin: '0 auto'}}>{props.data.description}</p>
            </div>

            <Publications articles={props.data.articles}/>
            <Cloud articles={props.data.articles}/>
            <Timeline articles={props.data.articles}/>
            <Claps articles={props.data.articles}/>
            <Stories articles={props.data.articles}/>
        </>
    )
}

