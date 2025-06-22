import {useState} from 'react'
import './App.css'
import axios from 'axios';
import type Data from "./models/Data.ts";
import Stats from "./Stats.tsx";

export default function App() {
    const [data, setData] = useState<Data>()
    const [alias, setAlias] = useState<string>('@')
    const [loading, setLoading] = useState<boolean>(false)

    const baseUrl = "http://localhost:7070"
    // const baseUrl = ""

    const fetchData = () => {
        setLoading(true)
        axios.get(`${baseUrl}/scrap/${alias}`)
            .then(response => {
                setLoading(false)
                setData(response.data)
            })
    }

    const reset = () => {
        setAlias('@')
        setData(undefined)
    }

    if (data) {
        return (
            <>
                <button style={{
                    position: "absolute",
                    top: "5px",
                    right: "5px",
                    color: "#506c2d",
                    background: "#fff",
                    border: "1px solid #506c2d"
                }} onClick={reset}>Load stats for some other
                    writer
                </button>
                <Stats data={data}/>
            </>
        )
    } else if (loading) {
        return (
            <div className="loader-container">
                <h1 className="loader-heading">Loading...</h1>
            </div>
        )
    } else {
        return (
            <>
                <h1>Fetch stats for a Medium writer</h1>
                <div style={{width: "100%", fontSize: "30pt"}}>
                    <span>Alias: </span>
                    <input style={{background: "#fff", color: "#506c2d", fontSize: "30pt"}} type={"text"}
                           value={alias} onChange={(event) => {
                        setAlias(event.target.value);
                    }} onKeyDown={(e) => {
                        if (e.key === 'Enter') {
                            fetchData()
                        }
                    }}/>
                    <div style={{textAlign: "center", paddingTop: "5px"}}>
                        <button style={{
                            background: "#fff",
                            color: "#506c2d",
                            fontSize: "80pt",
                            border: "1px solid #506c2d"
                        }}
                                onClick={fetchData}>Fetch
                        </button>
                    </div>
                </div>
            </>
        )
    }
}

