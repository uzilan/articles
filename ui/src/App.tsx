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
                setData(response.data)

                if (response.data.status === "ONGOING") {
                    setTimeout(() => {
                        fetchData()
                    }, 5000)
                } else {
                    setLoading(false)
                }
            })
    }

    const reset = () => {
        setAlias('@')
        setData(undefined)
    }

    if (data) {
        const count = data ? data.articles.length : 0
        let progress = <></>
        if (data) {
            if (data.status === "ONGOING") {
                progress = <div ><h5 className="loader-heading">Loading... {count} articles fetched</h5></div>
            } else {
                progress = <div><h5>Done loading, {count} articles fetched</h5></div>
            }
        }

        return (
            <>
                <div style={{
                    position: "absolute",
                    top: "5px",
                    right: "5px",

                }}>
                    <button style={{
                        color: "#506c2d",
                        background: "#fff",
                        border: "1px solid #506c2d"
                    }}
                            onClick={reset}>Load stats for some other writer
                    </button>
                    <div>
                        {progress}
                    </div>
                </div>
                <Stats data={data} alias={alias} />
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

