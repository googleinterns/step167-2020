import React/*, { useState, useEffect }*/ from "react";
import { Map, GoogleApiWrapper, Marker } from 'google-maps-react';
//import PropTypes from "prop-types";
//import { CBadge, CCard, CCardBody, CCardFooter, CCardHeader, CLink } from "@coreui/react";
//import CIcon from "@coreui/icons-react";
//import requestRoute from "../requests";
// import app from "firebase/app";
//import "firebase/auth";

const MapContainer = props => {
  //const recipes = props.recipes;

  const displayMarkers = () => {
    return props.recipes.map((recipe, i) => {
      return (
        <Marker
          key={i}
          id={i}
          position={{
            lat: recipe.location.latitude,
            lng: recipe.location.longitude,
          }}
          onClick={() => console.log("You clicked me!")}
        />
      );
    });
  };

  return (
    <Map google={props.google} zoom={8} style={mapStyles} initialCenter={{ lat: 47.444, lng: -122.176 }}>
      {displayMarkers()}
    </Map>
  );
};

const mapStyles = {
  width: '100%',
  height: '100%',
};

export default GoogleApiWrapper({
  apiKey: 'AIzaSyAe5HlFZFuhzMimXrKW1z3kglajbdHf_Rc'
})(MapContainer);
