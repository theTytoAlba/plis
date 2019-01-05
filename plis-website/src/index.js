import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';

/**
 * The fullscreen search mode, the initial page of the website.
 * Has the logo, title of product, search bar and protein/ligand selection.
 * Pings the Plis component back if query is updated.
 */
class FullscreenSearch extends React.Component {
    render() {
        return (
            <div className="vertical-flex match-parent">
                <img width="200px" height="200px" src="/boun_logo.png" alt="" />
                <p>Protein Ligand Interaction Search</p>
                <input type="text" placeholder="Your query" />
                <div className="horizontal-flex">
                    <div>Protein button</div>
                    <div>Ligand button</div>
                </div>
            </div>
        );
    }
}

/**
 * Holds the query in its state.
 * Decides between fullscreen design and results design.
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