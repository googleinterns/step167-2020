import React /*, { useState, useEffect }*/ from "react";
import { Map, GoogleApiWrapper, Marker } from "google-maps-react";
//import PropTypes from "prop-types";
//import { CBadge, CCard, CCardBody, CCardFooter, CCardHeader, CLink } from "@coreui/react";
//import CIcon from "@coreui/icons-react";
//import requestRoute from "../requests";
// import app from "firebase/app";
//import "firebase/auth";

const RecipeUploadMap = props => {
  //const recipes = props.recipes;
  /*
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
  }; */

  const displayMarker = () => {
    if (props.location) {
      return (
        <Marker
          position={{
            lat: props.location.latitude,
            lng: props.location.longitude,
          }}
          onClick={() => {
            props.setLocation(null);
            console.log("Marker clicked.");
          }}
        />
      );
    }
  };

  const mapClicked = (mapProps, map, clickEvent) => {
    const newCenter = {
      lat: clickEvent.latLng.lat(),
      lng: clickEvent.latLng.lng(),
    };
    const newLocation = {
      latitude: newCenter.lat,
      longitude: newCenter.lng,
    };
    props.setLocation(newLocation);
    map.setZoom(5);
    map.panTo(newCenter);
  };

  return (
    <Map google={props.google} zoom={2} style={mapStyles} initialCenter={{ lat: 0, lng: 0 }} onClick={mapClicked}>
      {displayMarker()}
    </Map>
  );
};

const mapStyles = {
  width: "95%",
  height: "100%",
};

export default GoogleApiWrapper({
  apiKey: "AIzaSyAe5HlFZFuhzMimXrKW1z3kglajbdHf_Rc",
})(RecipeUploadMap);
