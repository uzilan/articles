import type Props from "../models/props.ts";
import {type Word, WordCloud} from "@isoterik/react-word-cloud";

export default function Cloud(props: Props) {

    const ignore = ["the", "you", "your", "what", "when", "who", "was", "and", "with", "im", "ill", "this", "that", "thats", "for", "in", "to", "it", "its", "are", "is", "he", "she", "his", "her"]

    const words = props.articles
        .map(article => article.title.split(" "))
        .flat()
        .map(word => word.toLowerCase())
        .map(word => word.replace(/[^a-z]/g, ""))
        .filter(word => word.length > 2)
        .filter(word => !ignore.includes(word))

    const grouped = words.reduce<Record<string, number>>((acc, word) => {
        acc[word] = (acc[word] || 0) + 1;
        return acc;
    }, {})

    const groupedWords: Word[] = Object.entries(grouped).map((group) =>
        ({text: group[0], value: group[1] * 100}))


    return (
        <>
            <h2 style={{margin: '2rem'}}>Story title wordcloud</h2>
            <div style={{width: '100%', textAlign: 'center', display: 'flex', justifyContent: 'space-between'}}>
                <div style={{width: '25%'}}>&nbsp;</div>
                <div
                    style={{
                        textAlign: "center",
                        width: "500px",
                        height: "500px",

                    }}
                >
                    <WordCloud words={groupedWords} width={300} height={300} rotate={() => 0}/>
                </div>
                <div style={{width: '25%'}}>&nbsp;</div>
            </div>
        </>
    );
}
