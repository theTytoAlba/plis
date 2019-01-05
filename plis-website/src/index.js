import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';

/**
 * Renders a radiobutton with a text after it.
 * If user clicks on it while unselected, notifies parent.
 */
class RadioButton extends React.Component {
    onClick() {
        // If this button is not already selected, notify parent.
        if (!this.props.isSelected) { 
            this.props.onSelect(this.props.name);
        }
    }

    render() {
        return (
            <div className="horizontal-flex">
                <input type="radio" checked={this.props.isSelected} onClick={this.onClick.bind(this)}/> 
                <p>{this.props.name}</p>
            </div>
        );
    }
}

/**
 * The fullscreen search mode, the initial page of the website.
 * Has the logo, title of product, search bar and protein/ligand selection.
 * Pings the Plis component back if query is updated.
 */
class FullscreenSearch extends React.Component {
    constructor(props) {
        super(props);
        // Initially the query is of protein type and empty.
        this.state = {
            query: "",
            queryType: "Protein",
        };
    }

    /**
     * Is called by Protein button or Ligand button.
     * Only called if an unselected button is clicked.
     * Updates state.
     */
    handleQueryTypeButtonClick(selectedButtonName) {
        this.setState({queryType: selectedButtonName});
    }

    render() {
        return (
            <div className="vertical-flex match-parent">
                <img width="200px" height="200px" src="/boun_logo.png" alt="" />
                <p>Protein Ligand Interaction Search</p>
                <input type="text" placeholder="Your query" />
                <div className="horizontal-flex">
                    <RadioButton 
                        isSelected={this.state.queryType === "Protein"} 
                        name="Protein" 
                        onSelect={this.handleQueryTypeButtonClick.bind(this)}
                    />
                    <RadioButton 
                        isSelected={this.state.queryType === "Ligand"} 
                        name="Ligand" 
                        onSelect={this.handleQueryTypeButtonClick.bind(this)}
                    />
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