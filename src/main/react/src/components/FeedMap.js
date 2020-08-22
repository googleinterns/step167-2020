import React /*, { useState, useEffect }*/ from "react";
import { Map, GoogleApiWrapper, Marker } from "google-maps-react";
import PropTypes from "prop-types";
//import { CBadge, CCard, CCardBody, CCardFooter, CCardHeader, CLink } from "@coreui/react";
//import CIcon from "@coreui/icons-react";
//import requestRoute from "../requests";
// import app from "firebase/app";
//import "firebase/auth";

const FeedMap = props => {
  const displayMarkers = () => {
    return props.recipes.map((recipe, i) => {
      if (recipe.location && recipe.location.latitude && recipe.location.longitude) {
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
      } else {
        return null;
      }
    });
  };

  const mapClicked = (mapProps, map, clickEvent) => {
    const newCenter = {
      lat: clickEvent.latLng.lat(),
      lng: clickEvent.latLng.lng(),
    };
    map.panTo(newCenter);
  };

  return (
    <Map google={props.google} zoom={2} style={mapStyles} initialCenter={{ lat: 0, lng: 0 }} onClick={mapClicked}>
      {displayMarkers()}
    </Map>
  );
};

FeedMap.propTypes = {
  location: PropTypes.object,
  setLocation: PropTypes.func,
  google: PropTypes.object,
};

const mapStyles = {
  width: "97%",
  height: "100%",
};

export default GoogleApiWrapper({
  apiKey: "AIzaSyAe5HlFZFuhzMimXrKW1z3kglajbdHf_Rc",
})(FeedMap);
