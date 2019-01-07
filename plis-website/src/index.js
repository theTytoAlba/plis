import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import LinearProgress from '@material-ui/core/LinearProgress';

/**
 * Single interaction
 */
class Interaction extends React.Component {
    render() {        
        console.log(this.props);
        return(
            <div className="interaction">
                <div className="interaction-row">
                    <p className="interaction-attribute-name">Name:</p>
                    <p>{this.props.interaction.name}</p>
                </div>
                <div className="interaction-row">
                    <p className="interaction-attribute-name">Id:</p>
                    <p>{this.props.interaction.id}</p>
                </div>
                <p className="interaction-attribute-name">Affinity Findings</p>
            </div>
        )
    }
}

class QueryResultAttribute extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            showingMore: false
        }
    }

    onClick() {
        this.setState({showingMore: !this.state.showingMore});
    }

    render() {
        return(
            <div className="query-result-attribute">
                <p className="query-result-attribute-name">{this.props.name}</p>
                <div className="query-result-attribute-value">
                    <img src={this.state.showingMore ? "downarrow.png" : "rightarrow.png"} alt="" width="20px" height="20px" onClick={this.onClick.bind(this)}/>
                    <p className={this.state.showingMore ? "" : "shortened-text"}>{this.props.value}</p>
                </div>
            </div>
        )
    }
}


/**
 * The query result
 */
class QueryResult extends React.Component {
    render() {
        if (!this.props.result) {
            return(<div></div>);
        }
        
        return(
            <div className="query-result">
                <QueryResultAttribute name="Name" value={this.props.result.name}/>
                <QueryResultAttribute name="Id" value={this.props.result.id}/>
                {this.props.queryType === "Ligand" ? Object.keys(this.props.result.chemicalNames).map(
                    name => <QueryResultAttribute name={name} value={this.props.result.chemicalNames[name]} />) : ""}
            </div>
        )
    }
}

/**
 * The interactions list
 */
class InteractionsList extends React.Component {
    render() {
        return(
            <div className="interactions-list">
                <p className="interactions-title">Interactions</p>
                {Object.keys(this.props.interactions).map(index => <Interaction interaction={Object.values(this.props.interactions[index])[0]}/>)}
            </div>
        )
    }
}

/**
 * Results are:
 *   The interactions
 *   The main result of query
 */
class Results extends React.Component {
    render() {
        return(
            <div className="results-flex">
                <InteractionsList interactions={this.props.results.interactions} />
                <QueryResult result={this.props.results.result} queryType={this.props.queryType}/>
            </div>
        )
    }
}

/**
 * The results page contains:
 *  Search bar and query type
 *  Result of the query term
 *  List of interactions
 */
class ResultsScreen extends React.Component {
    onResultsReady(json) {
        this.setState({resultsReceived: true, results: json});
        console.log(json);
    }

    constructor(props) {
        super(props);
        this.state = {
            resultsReceived: false,
            results: {},
            query: props.query,
            queryType: props.queryType
        };

        fetch("http://localhost:60015", {
            method: "POST",
            body: JSON.stringify({
                query: this.props.query,
                queryType: this.props.queryType
            })
        })
            .then(response => response.json())
            .then(json => this.onResultsReady(json));
    }

    /**
     * Called by search bar.
     * Notifies parent with new query and query type. 
     */
    handleQuery(newQuery) {
        this.props.onQueryReady({query: newQuery, queryType: this.state.queryType});
    }

    render() {
        return (
            <div className="results-container">
                <div className="results-logo-and-title-container">
                    <img width="50px" height="50px" src="/boun_logo.png" alt=""/>
                    <p>Protein Ligand Interaction Search</p>
                </div>
                <div className="results-search-and-types-container">
                    <SearchBar query={this.props.query} onQuery={this.handleQuery.bind(this)}/>
                    <RadioButton 
                        isSelected={this.props.queryType === "Protein"} 
                        name="Protein" 
                    />
                    <RadioButton 
                        isSelected={this.props.queryType === "Ligand"} 
                        name="Ligand" 
                    />
                </div>
                <LinearProgress className={`linear-progress-bar ${this.state.resultsReceived ? "hidden" : ""}`}/>
                {this.state.resultsReceived ? <Results results={this.state.results} queryType={this.props.queryType}/> : ""}
            </div>
        );
    }
}

/**
 * Renders a search bar.
 * When user hits enter, types query is sent back to parent.
 */
class SearchBar extends React.Component {
    constructor(props) {
        super(props);
        // Value of query may be given, otherwise make it empty string.
        this.state = {
            query: this.props.query || ""
        };
    }

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
                    value={this.state.query}
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
        // Initially the query is empty and of type protein.
        this.state = {
            query: "",
            queryType: "Protein"
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
                <ResultsScreen onQueryReady={this.handleQuery.bind(this)} query={currentQuery} queryType={this.state.queryType}/>
            );
        }
    }
}

ReactDOM.render(<Plis />, document.getElementById('root'));