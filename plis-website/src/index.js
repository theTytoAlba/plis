import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';

class Search extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            query: "",
        };
    }

    render() {
        let currentQuery = this.state.query.trim();
        // If current query is empty, show the fullscreen search.
        if (currentQuery === "") {
            return (
                <div>
                    fullscreen search
                </div>
            );    
        } else {
            return (
                <div>
                    results search
                </div>
            );
    
        }
    }
}

ReactDOM.render(<Search />, document.getElementById('root'));