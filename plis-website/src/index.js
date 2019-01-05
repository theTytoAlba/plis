import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';

class FullscreenSearch extends React.Component {
    render() {
        return (
            <div className="vertical-flex">
                <img width="200px" height="200px" src="/boun_logo.png" alt=""/>
                <p>Protein Ligand Interaction Search</p>
            </div>
        );    
    }
}

/**
 * Holds the query in its state.
 * Decides between fulscreen design and results design.
 */
class Plis extends React.Component {
    constructor(props) {
        super(props);
        // Initially the query is empty.
        this.state = {
            query: "",
        };
    }

    render() {
        let currentQuery = this.state.query.trim();
        // If current query is empty, show the fullscreen search.
        if (currentQuery === "") {
            // Query is empty. Go for the fullscreen design.
            return (
                <FullscreenSearch />
            );
        } else {
            // Query there is a query, go for results page.
            return (
                <div>
                    results search
                </div>
            );
    
        }
    }
}

ReactDOM.render(<Plis />, document.getElementById('root'));