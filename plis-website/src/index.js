import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';

/**
 * The results page contains:
 *  Search bar and query type
 *  Result of the query term
 *  List of interactions
 */
class ResultsScreen extends React.Component {
    render() {
        return (
            <div className="results-container">
                <div className="results-logo-and-title-container">
                    <img width="50px" height="50px" src="/boun_logo.png" alt=""/>
                    <p>Protein Ligand Interaction Search</p>
                </div>
                <div className="results-search-and-types-container">
                    <SearchBar />
                </div>
            </div>
        );
    }
}

/**
 * Renders a search bar.
 * When user hits enter, types query is sent back to parent.
 */
class SearchBar extends React.Component {
    // Called when user makes a change in query.
    handleChange(e) {
        this.setState({query: e.target.value});
    }

    // Called when user hits enter. notifies parent with query.
    handleSubmit(e) {
        // Prevent reloading.
        e.preventDefault();
        // Let parent know about new query.
        this.props.onQuery(this.state.query);
    }

    render() {
        return (
            <form onSubmit={this.handleSubmit.bind(this)} className={`${this.props.fullscreenMode ? "fullscreen-search-bar-width" : "results-search-bar-width"}`}>
                <input 
                    className="search-bar"
                    type="text" 
                    placeholder="Your query"
                    onChange={this.handleChange.bind(this)}
                />
            </form>
        );
    }
}

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
            <div className="horizontal-flex radio-button">
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
     * Called by search bar.
     * Notifies parent with new query and query type. 
     */
    handleQuery(newQuery) {
        this.props.onQueryReady({query: newQuery, queryType: this.state.queryType});
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
            <div className="fullscreen-search-container">
                <img width="200px" height="200px" src="/boun_logo.png" alt="" />
                <p className="project-title">Protein Ligand Interaction Search</p>
                <SearchBar fullscreenMode={true} onQuery={this.handleQuery.bind(this)}/>
                <div className="query-type-buttons-container">
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

    handleQuery(queryInfo) {
        this.setState(queryInfo);
    }

    render() {
        let currentQuery = this.state.query.trim();
        // If current query is empty, show the fullscreen search.
        if (currentQuery === "") {
            // Query is empty. Go for the fullscreen design.
            return (
                <FullscreenSearch onQueryReady={this.handleQuery.bind(this)} />
            );
        } else {
            // Query there is a query, go for results page.
            return (
                <ResultsScreen />
            );

        }
    }
}

ReactDOM.render(<Plis />, document.getElementById('root'));