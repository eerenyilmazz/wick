---
name: Wick Design System
colors:
  surface: '#1e100d'
  surface-dim: '#1e100d'
  surface-bright: '#473531'
  surface-container-lowest: '#180b08'
  surface-container-low: '#271814'
  surface-container: '#2b1c18'
  surface-container-high: '#372622'
  surface-container-highest: '#42312d'
  on-surface: '#f9dcd6'
  on-surface-variant: '#e3beb6'
  inverse-surface: '#f9dcd6'
  inverse-on-surface: '#3e2c28'
  outline: '#aa8982'
  outline-variant: '#5b403a'
  surface-tint: '#ffb4a3'
  primary: '#ffb4a3'
  on-primary: '#630f00'
  primary-container: '#ff5a36'
  on-primary-container: '#5a0c00'
  inverse-primary: '#b52603'
  secondary: '#d1c1d9'
  on-secondary: '#372c3f'
  secondary-container: '#504559'
  on-secondary-container: '#c3b3cb'
  tertiary: '#5fd6f1'
  on-tertiary: '#003640'
  tertiary-container: '#00a0ba'
  on-tertiary-container: '#003039'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#ffdad2'
  primary-fixed-dim: '#ffb4a3'
  on-primary-fixed: '#3d0600'
  on-primary-fixed-variant: '#8c1900'
  secondary-fixed: '#eeddf6'
  secondary-fixed-dim: '#d1c1d9'
  on-secondary-fixed: '#211829'
  on-secondary-fixed-variant: '#4e4256'
  tertiary-fixed: '#aaedff'
  tertiary-fixed-dim: '#5fd6f1'
  on-tertiary-fixed: '#001f26'
  on-tertiary-fixed-variant: '#004e5c'
  background: '#1e100d'
  on-background: '#f9dcd6'
  surface-variant: '#42312d'
typography:
  headline-xl:
    fontFamily: Manrope
    fontSize: 40px
    fontWeight: '800'
    lineHeight: 48px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Manrope
    fontSize: 32px
    fontWeight: '800'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Manrope
    fontSize: 28px
    fontWeight: '800'
    lineHeight: 34px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Manrope
    fontSize: 24px
    fontWeight: '800'
    lineHeight: 32px
  body-lg:
    fontFamily: Manrope
    fontSize: 18px
    fontWeight: '500'
    lineHeight: 28px
  body-md:
    fontFamily: Manrope
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 24px
  label-md:
    fontFamily: Manrope
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.05em
  button-text:
    fontFamily: Manrope
    fontSize: 16px
    fontWeight: '700'
    lineHeight: 24px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  xs: 8px
  sm: 16px
  md: 24px
  lg: 32px
  xl: 48px
  gutter: 16px
  margin-mobile: 20px
  margin-desktop: 40px
---

## Brand & Style
The design system is engineered for a high-energy, social-first dating experience. It targets a Gen-Z demographic that values authenticity and immediate connection, drawing inspiration from the raw, high-contrast aesthetics of modern social platforms. 

The visual style is **Bold & Energetic**, characterized by solid fills, heavy typography, and a "vibrant dark" atmosphere. It avoids the pretense of luxury or soft gradients in favor of punchy, saturated colors and clear-cut geometry. The UI should feel fast, tactile, and unapologetically digital, evoking a sense of excitement and social urgency.

## Colors
The palette is centered around a high-visibility Primary Orange-Red that acts as the "spark" against a deep, structural purple background.

- **Primary (#FF5A36):** Used for all main actions, active states, and brand-critical elements. It is designed to pop aggressively against the dark canvases.
- **Surface (#241A2C):** The foundational color for cards, input fields, and containers. It provides enough contrast against the background to define shape without needing shadows.
- **Background (#0F0A14):** A solid, deep violet-black used for the base layout. Never use gradients on this surface.
- **Typography:** Headlines are pure white for maximum legibility. Secondary text and links use a pale grey-purple to maintain hierarchy while staying within the cool-toned spectrum of the background.

## Typography
Manrope is the exclusive typeface for the design system, chosen for its modern, geometric construction that feels both technical and friendly. 

The hierarchy is dominated by **Heavy Weight (800)** headlines with tight letter-spacing to create a "blocky," impactful look. All primary actions and titles should feel substantial. Body text uses a medium weight (500) to ensure readability against the dark background, while labels utilize a slightly bolder weight and uppercase styling to create clear functional signposts.

## Layout & Spacing
The layout follows a fluid-first approach with a focus on edge-to-edge content for mobile screens. 

- **Grid:** On mobile, use a 4-column layout with 20px side margins. On desktop, use a 12-column centered grid with a max-width of 1200px.
- **Rhythm:** Spacing is strictly based on a 4px baseline. Use 16px (sm) for internal element padding and 24px (md) for vertical spacing between sections.
- **Safe Areas:** Ensure content avoids the top notch and bottom home indicator, particularly for full-screen photo browsing common in dating interfaces.

## Elevation & Depth
This design system avoids traditional shadows and blurring effects to maintain a "flat-plus" aesthetic. Depth is achieved entirely through **Tonal Layering**:

1.  **Level 0 (Background):** #0F0A14 - The furthest back surface.
2.  **Level 1 (Cards/Inputs):** #241A2C - Elevated elements that sit on the background.
3.  **Level 2 (Active/Overlays):** Use the Primary color (#FF5A36) or a slightly lighter purple tint to indicate temporary focus.

Borders should be solid and used sparingly. When used, use a 2px stroke in the Primary color to highlight active states or "Matches."

## Shapes
The shape language is bold and intentional, utilizing "squircle" influences for containers and full-pill shapes for interactive triggers.

- **Containers & Cards:** Use an 18px (rounded-lg) corner radius for most UI blocks, including input fields and profile cards.
- **Action Elements:** Buttons must use a 23px radius (rounded-xl) to create a distinct, pill-shaped silhouette that separates them from informational cards.
- **Small Elements:** Chips and tags should use a 12px radius to maintain the "rounded square" aesthetic on a smaller scale.

## Components
Consistent styling of components ensures the "Wick" energy is felt throughout the app:

- **Buttons:** Primary buttons are solid Primary Color (#FF5A36) with Dark Brown-Black (#1A0800) text. They use a pill-shape (23px radius) and should feel massive and clickable.
- **Input Fields:** Styled using the Surface Color (#241A2C) with an 18px radius. Placeholder text should use the secondary text color. On focus, the border becomes a 2px solid Primary Color.
- **Cards:** Used for user profiles and chat threads. Solid Surface Color background, no shadows, 18px radius.
- **Chips/Badges:** Small interest tags (e.g., "Music," "Hiking") should have a solid Surface Color background with White text, using a 12px radius.
- **Checkboxes/Radios:** When selected, these should fill completely with the Primary Color, using a white checkmark or dot for contrast.
- **Lists:** Simple dividers using #241A2C (1px height) or clear vertical spacing (16px) between Surface-colored containers.